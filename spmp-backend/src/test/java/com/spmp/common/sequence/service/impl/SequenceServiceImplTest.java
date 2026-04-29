package com.spmp.common.sequence.service.impl;

import com.spmp.common.exception.BusinessException;
import com.spmp.common.sequence.config.SequenceProperties;
import com.spmp.common.sequence.constant.SequenceBizCode;
import com.spmp.common.sequence.domain.entity.SequenceGeneratorDO;
import com.spmp.common.sequence.metrics.SequenceMetrics;
import com.spmp.common.sequence.metrics.SequenceMeterNames;
import com.spmp.common.sequence.repository.SequenceGeneratorMapper;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SequenceServiceImpl} 单元测试。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class SequenceServiceImplTest {

    @Mock
    private SequenceGeneratorMapper sequenceGeneratorMapper;

    private SequenceProperties sequenceProperties;

    private SequenceServiceImpl sequenceService;

    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        sequenceProperties = new SequenceProperties();
        sequenceProperties.setStep(Collections.emptyMap());
        meterRegistry = new SimpleMeterRegistry();
        SequenceMetrics metrics = new SequenceMetrics(meterRegistry);
        sequenceService = new SequenceServiceImpl(sequenceGeneratorMapper, sequenceProperties, metrics);
        sequenceService.registerRemainingGauges();
    }

    @Test
    void nextSequence_allocatesSegmentThenUsesLocalCache() {
        String dateKey = "20260425";
        SequenceGeneratorDO row = new SequenceGeneratorDO();
        row.setId(1L);
        row.setBizCode(SequenceBizCode.PAYMENT);
        row.setDateKey(dateKey);
        row.setMaxAllocated(0L);
        row.setStep(1000);
        row.setVersion(0);
        row.setCreateTime(new Date());
        row.setUpdateTime(new Date());

        when(sequenceGeneratorMapper.selectByBizAndDate(SequenceBizCode.PAYMENT, dateKey))
                .thenReturn(null)
                .thenReturn(row);
        when(sequenceGeneratorMapper.initIfAbsent(anyString(), anyString(), anyInt())).thenReturn(1);
        when(sequenceGeneratorMapper.increaseByVersion(eq(1L), eq(0), eq(1000))).thenReturn(1);

        assertThat(sequenceService.nextSequence(SequenceBizCode.PAYMENT, dateKey)).isEqualTo(1L);
        assertThat(sequenceService.nextSequence(SequenceBizCode.PAYMENT, dateKey)).isEqualTo(2L);

        verify(sequenceGeneratorMapper, times(1)).increaseByVersion(eq(1L), eq(0), eq(1000));
    }

    @Test
    void nextSequence_throwsWhenAllocateExhaustsRetries() {
        String dateKey = "20260426";
        SequenceGeneratorDO row = new SequenceGeneratorDO();
        row.setId(2L);
        row.setMaxAllocated(5L);
        row.setVersion(1);

        when(sequenceGeneratorMapper.selectByBizAndDate(SequenceBizCode.WORK_ORDER, dateKey)).thenReturn(row);
        when(sequenceGeneratorMapper.increaseByVersion(eq(2L), eq(1), eq(200))).thenReturn(0);

        assertThatThrownBy(() -> sequenceService.nextSequence(SequenceBizCode.WORK_ORDER, dateKey))
                .isInstanceOf(BusinessException.class);
    }
}
