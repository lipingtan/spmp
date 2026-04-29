package com.spmp.base.domain.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 小区查询请求 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class CommunityQueryDTO {

    /** 小区名称（模糊查询） */
    private String communityName;

    /** 小区编码（模糊查询） */
    private String communityCode;

    /** 所属片区ID */
    private Long districtId;

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
