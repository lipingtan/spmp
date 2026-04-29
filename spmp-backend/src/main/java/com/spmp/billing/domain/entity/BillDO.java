package com.spmp.billing.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.spmp.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 账单主表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bl_bill")
public class BillDO extends BaseEntity {

    private String billNo;

    private String batchNo;

    private String feeType;

    private BigDecimal amount;

    private BigDecimal reduceAmount;

    private BigDecimal paidAmount;

    private BigDecimal refundAmount;

    private String status;

    private Long ownerId;

    private String ownerName;

    private Long houseId;

    private Long buildingId;

    private Long communityId;

    private String billingPeriod;

    private LocalDate dueDate;

    private BigDecimal usageAmount;

    private BigDecimal unitPrice;

    private BigDecimal houseArea;

    private LocalDateTime paidTime;

    private Byte reduceApproved;

    private String cancelReason;

    private String remark;
}
