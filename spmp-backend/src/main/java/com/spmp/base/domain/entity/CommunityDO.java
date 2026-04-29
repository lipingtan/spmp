package com.spmp.base.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.spmp.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 小区表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bs_community")
public class CommunityDO extends BaseEntity {

    /** 小区名称 */
    private String communityName;

    /** 小区编码（COM+6位序号） */
    private String communityCode;

    /** 小区地址 */
    private String address;

    /** 所属片区ID */
    private Long districtId;

    /** 联系电话 */
    private String phone;

    /** 物业公司名称 */
    private String propertyCompany;

    /** 小区面积（平方米） */
    private BigDecimal area;

    /** 经度 */
    private BigDecimal longitude;

    /** 纬度 */
    private BigDecimal latitude;

    /** 小区图片URL */
    private String image;

    /** 状态（0-启用 1-停用） */
    private Integer status;

    /** 备注 */
    private String remark;
}
