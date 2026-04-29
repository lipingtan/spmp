package com.spmp.base.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.spmp.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 房屋表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bs_house")
public class HouseDO extends BaseEntity {

    /** 房屋编号 */
    private String houseCode;

    /** 所属单元ID */
    private Long unitId;

    /** 楼层 */
    private Integer floor;

    /** 建筑面积（平方米） */
    private BigDecimal buildingArea;

    /** 使用面积（平方米） */
    private BigDecimal usableArea;

    /** 房屋状态（VACANT/OCCUPIED/RENTED/RENOVATING） */
    private String houseStatus;

    /** 房屋类型（RESIDENCE/SHOP/PARKING/OFFICE/OTHER） */
    private String houseType;

    /** 备注 */
    private String remark;
}
