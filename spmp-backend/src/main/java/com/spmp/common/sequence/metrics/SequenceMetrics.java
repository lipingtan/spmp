package com.spmp.common.sequence.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleSupplier;

/**
 * 序列分配 Micrometer 指标封装。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Component
public class SequenceMetrics {

    private final MeterRegistry meterRegistry;

    private final ConcurrentHashMap<String, Boolean> registeredRemainingGauges = new ConcurrentHashMap<>();

    public SequenceMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * 记录号段从数据库分配结果。
     *
     * @param bizCode 业务编码
     * @param success 是否成功
     */
    public void recordDbAllocate(String bizCode, boolean success) {
        try {
            meterRegistry.counter(SequenceMeterNames.ALLOCATE_TOTAL,
                    "bizCode", bizCode,
                    "result", success ? "success" : "fail").increment();
        } catch (Exception e) {
            log.warn("sequence metric record failed, name=allocate_total, bizCode={}", bizCode, e);
        }
    }

    /**
     * 记录号段分配耗时（毫秒）。
     *
     * @param bizCode 业务编码
     * @param millis  耗时
     */
    public void recordDbAllocateDuration(String bizCode, long millis) {
        try {
            meterRegistry.timer(SequenceMeterNames.ALLOCATE_DURATION, "bizCode", bizCode)
                    .record(millis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("sequence metric record failed, name=allocate_duration, bizCode={}", bizCode, e);
        }
    }

    /**
     * 注册「本地号段剩余」估算 Gauge（按实例、按 bizCode 汇总；同一 bizCode 仅注册一次）。
     *
     * @param bizCode   业务编码
     * @param remaining 剩余可发数量（非负）
     */
    public void registerLocalRemainingGauge(String bizCode, DoubleSupplier remaining) {
        if (registeredRemainingGauges.putIfAbsent(bizCode, Boolean.TRUE) != null) {
            return;
        }
        try {
            Gauge.builder(SequenceMeterNames.RANGE_REMAINING, remaining, DoubleSupplier::getAsDouble)
                    .tag("bizCode", bizCode)
                    .register(meterRegistry);
        } catch (Exception e) {
            registeredRemainingGauges.remove(bizCode);
            log.warn("sequence metric register gauge failed, name=range_remaining, bizCode={}", bizCode, e);
        }
    }
}
