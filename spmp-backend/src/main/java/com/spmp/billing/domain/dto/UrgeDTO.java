package com.spmp.billing.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 催收操作 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class UrgeDTO {

    @NotEmpty(message = "账单列表不能为空")
    private List<Long> billIds;

    @NotBlank(message = "催收方式不能为空")
    private String urgeType;

    private String feedback;
}
