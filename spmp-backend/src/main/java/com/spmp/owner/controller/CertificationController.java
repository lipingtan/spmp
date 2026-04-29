package com.spmp.owner.controller;

import com.spmp.common.result.PageResult;
import com.spmp.common.result.Result;
import com.spmp.owner.domain.dto.CertificationApproveDTO;
import com.spmp.owner.domain.dto.CertificationBatchApproveDTO;
import com.spmp.owner.domain.dto.CertificationQueryDTO;
import com.spmp.owner.domain.vo.CertificationVO;
import com.spmp.owner.service.CertificationService;
import com.spmp.user.annotation.OperationLog;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 认证审批管理 Controller（PC 管理端）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/owner/certifications")
@RequiredArgsConstructor
public class CertificationController {

    private final CertificationService certificationService;

    /**
     * 认证申请列表（分页查询，数据权限过滤）。
     */
    @GetMapping
    @PreAuthorize("@perm.check('owner:certify:list')")
    public PageResult<CertificationVO> listCertifications(CertificationQueryDTO queryDTO) {
        return certificationService.listCertifications(queryDTO);
    }

    /**
     * 单条审批。
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("@perm.check('owner:certify:approve')")
    @OperationLog(module = "认证审批", type = "UPDATE", description = "认证审批")
    public Result<Void> approveCertification(@PathVariable Long id,
                                              @Valid @RequestBody CertificationApproveDTO approveDTO) {
        certificationService.approveCertification(id, approveDTO);
        return Result.success(null);
    }

    /**
     * 批量审批。
     */
    @PutMapping("/batch-approve")
    @PreAuthorize("@perm.check('owner:certify:approve')")
    @OperationLog(module = "认证审批", type = "UPDATE", description = "批量认证审批")
    public Result<Void> batchApprove(@Valid @RequestBody CertificationBatchApproveDTO batchDTO) {
        certificationService.batchApprove(batchDTO);
        return Result.success(null);
    }
}
