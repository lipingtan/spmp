package com.spmp.billing.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 减免审批 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class BillReduceApproveDTO {

    @NotNull(message = "审批结果不能为空")
    private Boolean approved;

    private String approveRemark;
}
