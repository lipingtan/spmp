package com.spmp.base.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 单元新增请求 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class UnitCreateDTO {

    /** 单元名称 */
    @NotBlank(message = "单元名称不能为空")
    @Size(max = 64, message = "单元名称不能超过64个字符")
    private String unitName;

    /** 单元编号 */
    @NotBlank(message = "单元编号不能为空")
    @Size(max = 32, message = "单元编号不能超过32个字符")
    private String unitCode;

    /** 所属楼栋ID */
    @NotNull(message = "所属楼栋不能为空")
    private Long buildingId;

    /** 备注 */
    private String remark;
}
