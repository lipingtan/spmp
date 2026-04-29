package com.spmp.billing.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 批量生成账单 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class BillGenerateDTO {

    @NotBlank(message = "费用类型不能为空")
    private String feeType;

    @NotBlank(message = "账期不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "账期格式为 yyyy-MM")
    private String billingPeriod;

    private Long buildingId;

    private Long unitId;
}
