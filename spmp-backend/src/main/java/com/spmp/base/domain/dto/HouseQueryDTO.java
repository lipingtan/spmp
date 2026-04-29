package com.spmp.base.domain.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 房屋查询请求 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class HouseQueryDTO {

    /** 房屋编号（模糊查询） */
    private String houseCode;

    /** 所属小区ID（支持按小区筛选，需 JOIN） */
    private Long communityId;

    /** 所属楼栋ID（支持按楼栋筛选，需 JOIN） */
    private Long buildingId;

    /** 所属单元ID */
    private Long unitId;

    /** 楼层 */
    private Integer floor;

    /** 房屋状态 */
    private String houseStatus;

    /** 房屋类型 */
    private String houseType;

    /** 当前页码 */
    @Min(1)
    private Integer pageNum = 1;

    /** 每页条数 */
    @Min(1)
    @Max(100)
    private Integer pageSize = 10;
}
