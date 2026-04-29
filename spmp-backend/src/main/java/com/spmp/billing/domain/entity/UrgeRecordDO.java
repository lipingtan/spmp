package com.spmp.billing.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.spmp.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 催收记录表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bl_urge_record")
public class UrgeRecordDO extends BaseEntity {

    private Long billId;

    private String billNo;

    private String urgeType;

    private Long urgeUserId;

    private String urgeUserName;

    private LocalDateTime urgeTime;

    private String feedback;
}
