package com.spmp.billing.domain.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 账单减免申请 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class BillReduceDTO {

    @NotNull(message = "减免金额不能为空")
    @DecimalMin(value = "0.01")
    private BigDecimal reduceAmount;

    @NotBlank(message = "减免原因不能为空")
    @Size(max = 512)
    private String reason;
}
