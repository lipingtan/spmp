package com.spmp.owner.domain.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 认证申请查询 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class CertificationQueryDTO {

    /** 认证状态筛选 */
    private String certStatus;

    /** 小区筛选 */
    private Long communityId;

    /** 楼栋筛选 */
    private Long buildingId;

    /** 当前页码 */
    @Min(1)
    private Integer pageNum = 1;

    /** 每页条数 */
    @Min(1)
    @Max(100)
    private Integer pageSize = 10;
}
