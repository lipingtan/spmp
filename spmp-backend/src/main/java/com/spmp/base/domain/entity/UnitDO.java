package com.spmp.base.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.spmp.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 单元表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bs_unit")
public class UnitDO extends BaseEntity {

    /** 单元名称 */
    private String unitName;

    /** 单元编号 */
    private String unitCode;

    /** 所属楼栋ID */
    private Long buildingId;

    /** 状态（0-启用 1-停用） */
    private Integer status;

    /** 备注 */
    private String remark;
}
