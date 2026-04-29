package com.spmp.billing.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 收费统计看板 VO。
 */
@Data
public class StatisticsVO {
    private BigDecimal totalReceivable;
    private BigDecimal totalReceived;
    private BigDecimal collectionRate;
    private BigDecimal overdueAmount;
    private Integer overdueCount;
    private BigDecimal reduceAmount;
    private List<TrendDataVO> trends;
}
