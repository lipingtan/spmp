package com.spmp.base.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 小区分页查询结果 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class CommunityPageDTO {

    /** 主键ID */
    private Long id;

    /** 小区名称 */
    private String communityName;

    /** 小区编码 */
    private String communityCode;

    /** 小区地址 */
    private String address;

    /** 所属片区ID */
    private Long districtId;

    /** 所属片区名称（关联查询） */
    private String districtName;

    /** 联系电话 */
    private String phone;

    /** 物业公司名称 */
    private String propertyCompany;

    /** 小区面积（平方米） */
    private BigDecimal area;

    /** 状态（0-启用 1-停用） */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 总户数（统计） */
    private Integer houseCount;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
