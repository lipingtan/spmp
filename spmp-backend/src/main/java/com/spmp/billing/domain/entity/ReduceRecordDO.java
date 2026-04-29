package com.spmp.billing.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.spmp.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 减免记录表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bl_reduce_record")
public class ReduceRecordDO extends BaseEntity {

    private Long billId;

    private String billNo;

    private BigDecimal reduceAmount;

    private String reason;

    private String status;

    private Long applicantId;

    private String applicantName;

    private Long approverId;

    private String approverName;

    private LocalDateTime approveTime;

    private String approveRemark;
}
