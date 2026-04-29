package com.spmp.base.domain.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 楼栋新增请求 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class BuildingCreateDTO {

    /** 楼栋名称 */
    @NotBlank(message = "楼栋名称不能为空")
    @Size(max = 64, message = "楼栋名称不能超过64个字符")
    private String buildingName;

    /** 楼栋编号 */
    @NotBlank(message = "楼栋编号不能为空")
    @Size(max = 32, message = "楼栋编号不能超过32个字符")
    private String buildingCode;

    /** 所属小区ID */
    @NotNull(message = "所属小区不能为空")
    private Long communityId;

    /** 地上层数 */
    @NotNull(message = "地上层数不能为空")
    @Min(value = 1, message = "地上层数至少为1")
    private Integer aboveGroundFloors;

    /** 地下层数 */
    @NotNull(message = "地下层数不能为空")
    @Min(value = 0, message = "地下层数不能为负数")
    private Integer undergroundFloors;

    /** 每层户数 */
    @NotNull(message = "每层户数不能为空")
    @Min(value = 1, message = "每层户数至少为1")
    private Integer unitsPerFloor;

    /** 楼栋类型（RESIDENTIAL/COMMERCIAL/GARAGE/MIXED/OTHER） */
    @NotBlank(message = "楼栋类型不能为空")
    private String buildingType;

    /** 备注 */
    private String remark;
}
