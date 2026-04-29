package com.spmp.base.domain.dto;

import lombok.Data;

import java.util.Date;

/**
 * 楼栋分页查询结果 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class BuildingPageDTO {

    /** 主键ID */
    private Long id;

    /** 楼栋名称 */
    private String buildingName;

    /** 楼栋编号 */
    private String buildingCode;

    /** 所属小区ID */
    private Long communityId;

    /** 所属小区名称（关联查询） */
    private String communityName;

    /** 地上层数 */
    private Integer aboveGroundFloors;

    /** 地下层数 */
    private Integer undergroundFloors;

    /** 每层户数 */
    private Integer unitsPerFloor;

    /** 楼栋类型 */
    private String buildingType;

    /** 状态（0-启用 1-停用） */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
