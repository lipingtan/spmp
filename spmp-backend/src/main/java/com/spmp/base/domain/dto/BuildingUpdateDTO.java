package com.spmp.base.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 楼栋编辑请求 DTO。
 * <p>
 * 注意：不包含 communityId（禁止修改所属小区）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class BuildingUpdateDTO {

    /** 楼栋名称 */
    @NotBlank(message = "楼栋名称不能为空")
    private String buildingName;

    /** 楼栋编号 */
    @NotBlank(message = "楼栋编号不能为空")
    private String buildingCode;

    /** 地上层数 */
    private Integer aboveGroundFloors;

    /** 地下层数 */
    private Integer undergroundFloors;

    /** 每层户数 */
    private Integer unitsPerFloor;

    /** 楼栋类型 */
    private String buildingType;

    /** 备注 */
    private String remark;
}
