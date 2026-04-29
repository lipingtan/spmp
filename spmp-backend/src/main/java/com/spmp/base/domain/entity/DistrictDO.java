package com.spmp.base.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.spmp.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 片区表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bs_district")
public class DistrictDO extends BaseEntity {

    /** 片区名称 */
    private String districtName;

    /** 片区编码（DIS+6位序号） */
    private String districtCode;

    /** 状态（0-启用 1-停用） */
    private Integer status;

    /** 备注 */
    private String remark;
}
