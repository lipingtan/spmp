package com.spmp.billing.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 账单详情 VO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class BillDetailVO {

    private Long id;
    private String billNo;
    private String batchNo;
    private String feeType;
    private String feeTypeName;
    private String billingMethod;
    private BigDecimal amount;
    private BigDecimal reduceAmount;
    private BigDecimal paidAmount;
    private BigDecimal refundAmount;
    private String status;
    private String statusName;
    private Long ownerId;
    private String ownerName;
    private Long houseId;
    private String houseFullName;
    private Long communityId;
    private String communityName;
    private Long buildingId;
    private String buildingName;
    private String billingPeriod;
    private LocalDate dueDate;
    private BigDecimal usageAmount;
    private BigDecimal unitPrice;
    private BigDecimal houseArea;
    private LocalDateTime paidTime;
    private String remark;
    private Date createTime;
    private List<ReduceRecordVO> reduceRecords;
    private List<UrgeRecordVO> urgeRecords;
    private List<RefundRecordVO> refundRecords;
}
