package com.spmp.billing.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款记录 VO。
 */
@Data
public class RefundRecordVO {
    private Long id;
    private String billNo;
    private BigDecimal refundAmount;
    private String refundReason;
    private String operatorName;
    private LocalDateTime refundTime;
}
