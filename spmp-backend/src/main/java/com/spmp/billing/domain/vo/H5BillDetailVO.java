package com.spmp.billing.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * H5 端账单详情 VO。
 */
@Data
public class H5BillDetailVO {
    private Long id;
    private String billNo;
    private String feeType;
    private String feeTypeName;
    private String billingMethod;
    private BigDecimal amount;
    private BigDecimal reduceAmount;
    private BigDecimal payAmount;
    private String status;
    private String statusName;
    private String houseFullName;
    private String billingPeriod;
    private LocalDate dueDate;
    private BigDecimal usageAmount;
    private BigDecimal unitPrice;
    private BigDecimal houseArea;
    private LocalDateTime paidTime;
    private Date createTime;
    private String remark;
    private List<PaymentVO> payments;
    private List<ReduceRecordVO> reduceRecords;
}
