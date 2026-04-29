package com.spmp.billing.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 支付记录 VO。
 */
@Data
public class PaymentVO {
    private Long id;
    private String paymentNo;
    private BigDecimal totalAmount;
    private String payMethod;
    private String payMethodName;
    private String status;
    private String statusName;
    private LocalDateTime paidTime;
    private Date createTime;
}
