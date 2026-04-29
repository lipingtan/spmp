package com.spmp.workorder.config;

import com.spmp.common.exception.BusinessException;
import com.spmp.common.exception.ErrorCode;
import com.spmp.common.util.RedisUtils;
import com.spmp.common.sequence.config.SequenceProperties;
import com.spmp.common.sequence.constant.SequenceBizCode;
import com.spmp.common.sequence.service.SequenceService;
import com.spmp.workorder.constant.WorkOrderConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 工单编号生成器（支持 Redis 自增 legacy 与 DB 号段 segment 两种模式）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNoGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.BASIC_ISO_DATE;

    private final RedisUtils redisUtils;
    private final SequenceService sequenceService;
    private final SequenceProperties sequenceProperties;

    public String generate() {
        String today = LocalDate.now().format(DATE_FMT);
        if ("segment".equalsIgnoreCase(sequenceProperties.getMode().getWorkorder())) {
            long seq = sequenceService.nextSequence(SequenceBizCode.WORK_ORDER, today);
            return WorkOrderConstants.ORDER_NO_PREFIX + today + String.format("%04d", seq);
        }
        String key = WorkOrderConstants.SEQ_KEY_PREFIX + today;
        Long seq = redisUtils.increment(key, 1);
        redisUtils.expire(key, 25, TimeUnit.HOURS);
        if (seq == null || seq <= 0L) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "工单号生成失败，请稍后重试");
        }
        return WorkOrderConstants.ORDER_NO_PREFIX + today + String.format("%04d", seq);
    }
}
