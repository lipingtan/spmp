package com.spmp.billing.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新费用配置 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class FeeConfigUpdateDTO {

    private String billingMethod;

    private BigDecimal unitPrice;

    private Integer dueDay;

    private String status;

    private String remark;
}
