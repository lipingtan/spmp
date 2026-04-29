package com.spmp.base.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 单元编辑请求 DTO。
 * <p>
 * 注意：不包含 buildingId（禁止修改所属楼栋）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class UnitUpdateDTO {

    /** 单元名称 */
    @NotBlank(message = "单元名称不能为空")
    private String unitName;

    /** 单元编号 */
    @NotBlank(message = "单元编号不能为空")
    private String unitCode;

    /** 备注 */
    private String remark;
}
