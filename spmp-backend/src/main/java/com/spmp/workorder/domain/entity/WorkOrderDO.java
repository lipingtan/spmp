package com.spmp.workorder.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.spmp.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 工单主表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wo_work_order")
public class WorkOrderDO extends BaseEntity {

    private String orderNo;

    private String orderType;

    private String addressType;

    private Long communityId;

    private Long houseId;

    private Long buildingId;

    private Long unitId;

    private Long reporterId;

    private String reporterName;

    private String reporterPhone;

    private String description;

    private String status;

    private Long repairUserId;

    private Integer rejectCount;

    private Integer urgeCount;

    private LocalDateTime lastUrgeTime;

    private LocalDateTime expectedCompleteTime;

    private LocalDateTime actualStartTime;

    private LocalDateTime actualCompleteTime;

    private Integer repairDuration;

    private String cancelReason;

    private Long cancelBy;
}
