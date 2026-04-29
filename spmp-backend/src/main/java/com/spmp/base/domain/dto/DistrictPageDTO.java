package com.spmp.base.domain.dto;

import lombok.Data;

import java.util.Date;

/**
 * 片区分页查询结果 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class DistrictPageDTO {

    /** 主键ID */
    private Long id;

    /** 片区名称 */
    private String districtName;

    /** 片区编码 */
    private String districtCode;

    /** 状态（0-启用 1-停用） */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 下属小区数量 */
    private Integer communityCount;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
