package com.spmp.billing.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * PC端账单列表 VO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class BillListVO {

    private Long id;
    private String billNo;
    private String feeType;
    private String feeTypeName;
    private BigDecimal amount;
    private BigDecimal reduceAmount;
    private BigDecimal paidAmount;
    private String status;
    private String statusName;
    private Long ownerId;
    private String ownerName;
    private Long houseId;
    private String houseFullName;
    private String billingPeriod;
    private LocalDate dueDate;
    private LocalDateTime paidTime;
    private String batchNo;
    private LocalDateTime createTime;
}
