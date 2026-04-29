package com.spmp.base.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.spmp.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 楼栋表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bs_building")
public class BuildingDO extends BaseEntity {

    /** 楼栋名称 */
    private String buildingName;

    /** 楼栋编号 */
    private String buildingCode;

    /** 所属小区ID */
    private Long communityId;

    /** 地上层数 */
    private Integer aboveGroundFloors;

    /** 地下层数 */
    private Integer undergroundFloors;

    /** 每层户数 */
    private Integer unitsPerFloor;

    /** 楼栋类型（RESIDENTIAL/COMMERCIAL/GARAGE/MIXED/OTHER） */
    private String buildingType;

    /** 状态（0-启用 1-停用） */
    private Integer status;

    /** 备注 */
    private String remark;
}
