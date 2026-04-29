package com.spmp.owner.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 认证审批请求 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class CertificationApproveDTO {

    /** 审批结果（APPROVE-通过 / REJECT-驳回） */
    @NotBlank(message = "审批结果不能为空")
    private String action;

    /** 驳回原因（驳回时必填） */
    private String rejectReason;
}
