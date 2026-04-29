package com.spmp.owner.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 批量认证审批请求 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class CertificationBatchApproveDTO {

    /** 审批ID列表 */
    @NotEmpty(message = "审批ID列表不能为空")
    @Size(max = 100, message = "单次批量审批最多100条")
    private List<Long> ids;

    /** 审批结果（APPROVE-通过 / REJECT-驳回） */
    @NotBlank(message = "审批结果不能为空")
    private String action;

    /** 驳回原因（驳回时必填） */
    private String rejectReason;
}
