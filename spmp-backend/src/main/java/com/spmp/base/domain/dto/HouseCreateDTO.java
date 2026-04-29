package com.spmp.base.domain.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 房屋新增请求 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class HouseCreateDTO {

    /** 房屋编号 */
    @NotBlank(message = "房屋编号不能为空")
    @Size(max = 32, message = "房屋编号不能超过32个字符")
    private String houseCode;

    /** 所属单元ID */
    @NotNull(message = "所属单元不能为空")
    private Long unitId;

    /** 楼层 */
    @NotNull(message = "楼层不能为空")
    private Integer floor;

    /** 建筑面积（平方米） */
    @NotNull(message = "建筑面积不能为空")
    @DecimalMin(value = "0.01", message = "建筑面积必须大于0")
    private BigDecimal buildingArea;

    /** 使用面积（平方米） */
    private BigDecimal usableArea;

    /** 房屋状态（VACANT/OCCUPIED/RENTED/RENOVATING） */
    @NotBlank(message = "房屋状态不能为空")
    private String houseStatus;

    /** 房屋类型（RESIDENCE/SHOP/PARKING/OFFICE/OTHER） */
    @NotBlank(message = "房屋类型不能为空")
    private String houseType;

    /** 备注 */
    private String remark;
}
