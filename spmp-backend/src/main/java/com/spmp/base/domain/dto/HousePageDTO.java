package com.spmp.base.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 房屋分页查询结果 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class HousePageDTO {

    /** 主键ID */
    private Long id;

    /** 房屋编号 */
    private String houseCode;

    /** 所属单元ID */
    private Long unitId;

    /** 所属单元名称（关联查询） */
    private String unitName;

    /** 所属楼栋ID（关联查询） */
    private Long buildingId;

    /** 所属楼栋名称（关联查询） */
    private String buildingName;

    /** 所属小区ID（关联查询） */
    private Long communityId;

    /** 所属小区名称（关联查询） */
    private String communityName;

    /** 楼层 */
    private Integer floor;

    /** 建筑面积（平方米） */
    private BigDecimal buildingArea;

    /** 使用面积（平方米） */
    private BigDecimal usableArea;

    /** 房屋状态 */
    private String houseStatus;

    /** 房屋类型 */
    private String houseType;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
