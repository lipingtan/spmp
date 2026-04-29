package com.spmp.owner.domain.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 业主列表查询 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class OwnerQueryDTO {

    /** 姓名模糊搜索 */
    private String ownerName;

    /** 手机号搜索（加密后精确匹配） */
    private String phone;

    /** 状态筛选 */
    private String ownerStatus;

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
