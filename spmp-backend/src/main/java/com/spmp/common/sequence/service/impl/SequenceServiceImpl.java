package com.spmp.common.sequence.service.impl;

import com.spmp.common.exception.BusinessException;
import com.spmp.common.exception.ErrorCode;
import com.spmp.common.sequence.config.SequenceProperties;
import com.spmp.common.sequence.constant.SequenceBizCode;
import com.spmp.common.sequence.domain.entity.SequenceGeneratorDO;
import com.spmp.common.sequence.domain.model.SequenceRange;
import com.spmp.common.sequence.metrics.SequenceMetrics;
import com.spmp.common.sequence.repository.SequenceGeneratorMapper;
import com.spmp.common.sequence.service.SequenceService;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 序列服务实现。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SequenceServiceImpl implements SequenceService {

    private static final int MAX_RETRY_TIMES = 3;

    private final SequenceGeneratorMapper sequenceGeneratorMapper;
    private final SequenceProperties sequenceProperties;
    private final SequenceMetrics sequenceMetrics;

    private final Map<String, LocalRangeState> localRanges = new ConcurrentHashMap<>();

    @PostConstruct
    void registerRemainingGauges() {
        sequenceMetrics.registerLocalRemainingGauge(SequenceBizCode.PAYMENT, () -> sumLocalRemainingAcrossDateKeys(SequenceBizCode.PAYMENT));
        sequenceMetrics.registerLocalRemainingGauge(SequenceBizCode.WORK_ORDER, () -> sumLocalRemainingAcrossDateKeys(SequenceBizCode.WORK_ORDER));
    }

    private double sumLocalRemainingAcrossDateKeys(String bizCode) {
        String prefix = bizCode + ":";
        long sum = 0L;
        for (Map.Entry<String, LocalRangeState> e : localRanges.entrySet()) {
            if (e.getKey().startsWith(prefix)) {
                sum += e.getValue().remainingCount();
            }
        }
        return sum;
    }

    @Override
    public long nextSequence(String bizCode, String dateKey) {
        String rangeKey = buildRangeKey(bizCode, dateKey);
        LocalRangeState state = localRanges.computeIfAbsent(rangeKey, key -> new LocalRangeState());

        Long next = state.nextIfAvailable();
        if (next != null) {
            if (log.isDebugEnabled()) {
                log.debug("sequence hit local segment, bizCode={}, dateKey={}, value={}", bizCode, dateKey, next);
            }
            return next;
        }

        synchronized (state) {
            next = state.nextIfAvailable();
            if (next != null) {
                return next;
            }
            SequenceRange range = allocateRange(bizCode, dateKey);
            state.reset(range);
            Long value = state.nextIfAvailable();
            if (value == null) {
                throw new BusinessException(ErrorCode.OPERATION_FAILED, "序列分配失败，请稍后重试");
            }
            return value;
        }
    }

    private SequenceRange allocateRange(String bizCode, String dateKey) {
        int step = resolveStep(bizCode);
        long startNanos = System.nanoTime();

        for (int retry = 0; retry < MAX_RETRY_TIMES; retry++) {
            SequenceGeneratorDO record = sequenceGeneratorMapper.selectByBizAndDate(bizCode, dateKey);
            if (record == null) {
                sequenceGeneratorMapper.initIfAbsent(bizCode, dateKey, step);
                log.warn("sequence row missing, initialized, bizCode={}, dateKey={}, retry={}", bizCode, dateKey, retry);
                continue;
            }

            int rows = sequenceGeneratorMapper.increaseByVersion(record.getId(), record.getVersion(), step);
            if (rows == 1) {
                long start = record.getMaxAllocated() + 1L;
                long end = record.getMaxAllocated() + step;
                long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
                sequenceMetrics.recordDbAllocate(bizCode, true);
                sequenceMetrics.recordDbAllocateDuration(bizCode, elapsedMs);
                log.info("sequence range allocated, bizCode={}, dateKey={}, rangeStart={}, rangeEnd={}, step={}, retry={}, elapsedMs={}",
                        bizCode, dateKey, start, end, step, retry, elapsedMs);
                return new SequenceRange(start, end);
            }
            log.warn("sequence version conflict, bizCode={}, dateKey={}, retry={}", bizCode, dateKey, retry);
        }

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        sequenceMetrics.recordDbAllocate(bizCode, false);
        sequenceMetrics.recordDbAllocateDuration(bizCode, elapsedMs);
        log.error("sequence range allocate failed, bizCode={}, dateKey={}, elapsedMs={}", bizCode, dateKey, elapsedMs);
        throw new BusinessException(ErrorCode.OPERATION_FAILED, "序列分配失败，请稍后重试");
    }

    private int resolveStep(String bizCode) {
        Integer configured = sequenceProperties.getStep().get(bizCode);
        if (configured != null && configured > 0) {
            return configured;
        }
        if (SequenceBizCode.WORK_ORDER.equals(bizCode)) {
            return 200;
        }
        return 1000;
    }

    private String buildRangeKey(String bizCode, String dateKey) {
        return bizCode + ":" + dateKey;
    }

    private static class LocalRangeState {
        private final AtomicLong cursor = new AtomicLong(0L);
        private volatile long end = 0L;

        private void reset(SequenceRange range) {
            this.cursor.set(range.getStart());
            this.end = range.getEnd();
        }

        private Long nextIfAvailable() {
            long value = cursor.getAndIncrement();
            if (value <= 0 || value > end) {
                return null;
            }
            return value;
        }

        /**
         * 当前本地号段剩余可发数量（未初始化或已耗尽为 0）。
         */
        private long remainingCount() {
            long nextCandidate = cursor.get();
            long rangeEnd = this.end;
            if (rangeEnd <= 0L || nextCandidate <= 0L) {
                return 0L;
            }
            return Math.max(0L, rangeEnd - nextCandidate + 1L);
        }
    }
}
