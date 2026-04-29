package com.spmp.billing.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 取消账单 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class BillCancelDTO {

    @NotBlank(message = "取消原因不能为空")
    @Size(max = 512)
    private String cancelReason;
}
