package com.spmp.owner.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.spmp.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 认证申请表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ow_certification")
public class CertificationDO extends BaseEntity {

    /** 业主ID */
    private Long ownerId;

    /** 房屋ID（关联 bs_house.id） */
    private Long houseId;

    /** 认证状态（PENDING-待审批 APPROVED-已通过 REJECTED-已驳回） */
    private String certStatus;

    /** 申请时间 */
    private Date applyTime;

    /** 审批时间 */
    private Date approveTime;

    /** 审批人ID */
    private Long approverId;

    /** 驳回原因 */
    private String rejectReason;
}
