package com.spmp.billing.domain.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 创建费用配置 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class FeeConfigCreateDTO {

    @NotNull(message = "小区不能为空")
    private Long communityId;

    private Long buildingId;

    @NotBlank(message = "费用类型不能为空")
    private String feeType;

    @NotBlank(message = "计费方式不能为空")
    private String billingMethod;

    @NotNull(message = "单价不能为空")
    @DecimalMin(value = "0.0001")
    private BigDecimal unitPrice;

    @Min(1)
    @Max(28)
    private Integer dueDay;

    private String remark;
}
