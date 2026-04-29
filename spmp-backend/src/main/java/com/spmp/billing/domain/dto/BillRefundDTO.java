package com.spmp.billing.domain.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 账单退款 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class BillRefundDTO {

    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01")
    private BigDecimal refundAmount;

    @NotBlank(message = "退款原因不能为空")
    @Size(max = 512)
    private String refundReason;
}
