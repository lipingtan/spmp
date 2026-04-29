package com.spmp.base.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 房屋简要信息 DTO（对外接口）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class HouseBriefDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 房屋ID */
    private Long id;

    /** 房屋编号 */
    private String houseCode;

    /** 所属单元ID */
    private Long unitId;

    /** 楼层 */
    private Integer floor;

    /** 建筑面积（平方米） */
    private BigDecimal buildingArea;

    /** 房屋状态 */
    private String houseStatus;

    /** 房屋类型 */
    private String houseType;
}
