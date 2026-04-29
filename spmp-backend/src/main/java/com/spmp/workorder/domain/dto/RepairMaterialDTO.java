package com.spmp.workorder.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 维修材料 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class RepairMaterialDTO {

    @NotBlank(message = "材料名称不能为空")
    private String materialName;

    @NotNull(message = "数量不能为空")
    private BigDecimal quantity;

    private String unit;

    @NotNull(message = "单价不能为空")
    private BigDecimal unitPrice;
}
