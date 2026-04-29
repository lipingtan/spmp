package com.spmp.base.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 片区新增请求 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class DistrictCreateDTO {

    /** 片区名称 */
    @NotBlank(message = "片区名称不能为空")
    @Size(max = 64, message = "片区名称不能超过64个字符")
    private String districtName;

    /** 备注 */
    @Size(max = 256, message = "备注不能超过256个字符")
    private String remark;
}
