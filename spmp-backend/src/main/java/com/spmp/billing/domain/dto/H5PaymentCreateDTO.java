package com.spmp.billing.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * H5 端创建支付 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class H5PaymentCreateDTO {

    @NotEmpty(message = "请选择要支付的账单")
    private List<Long> billIds;

    @NotBlank(message = "支付方式不能为空")
    private String payMethod;
}
