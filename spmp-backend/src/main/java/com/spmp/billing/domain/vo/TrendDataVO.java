package com.spmp.billing.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 收费趋势数据 VO。
 */
@Data
public class TrendDataVO {
    private String period;
    private BigDecimal receivable;
    private BigDecimal received;
    private BigDecimal collectionRate;
}
