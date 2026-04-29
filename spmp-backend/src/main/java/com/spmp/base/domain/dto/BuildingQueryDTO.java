package com.spmp.base.domain.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 楼栋查询请求 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class BuildingQueryDTO {

    /** 楼栋名称（模糊查询） */
    private String buildingName;

    /** 楼栋编号（模糊查询） */
    private String buildingCode;

    /** 所属小区ID */
    private Long communityId;

    /** 楼栋类型 */
    private String buildingType;

    /** 状态（0-启用 1-停用） */
    private Integer status;

    /** 当前页码 */
    @Min(1)
    private Integer pageNum = 1;

    /** 每页条数 */
    @Min(1)
    @Max(100)
    private Integer pageSize = 10;
}
