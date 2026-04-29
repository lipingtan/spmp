package com.spmp.owner.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.spmp.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 房产绑定表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ow_property_binding")
public class PropertyBindingDO extends BaseEntity {

    /** 业主ID */
    private Long ownerId;

    /** 房屋ID（关联 bs_house.id） */
    private Long houseId;

    /** 关系类型（OWNER-业主 TENANT-租户 FAMILY-家属） */
    private String relationType;

    /** 绑定时间 */
    private Date bindingTime;

    /** 解绑时间 */
    private Date unbindingTime;

    /** 绑定状态（0-有效 1-已解绑） */
    private Integer status;
}
