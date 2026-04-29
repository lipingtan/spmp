package com.spmp.owner.controller.h5;

import com.spmp.common.result.Result;
import com.spmp.common.util.SecurityUtils;
import com.spmp.owner.domain.dto.H5CertificationCreateDTO;
import com.spmp.owner.domain.vo.CertificationVO;
import com.spmp.owner.service.CertificationService;
import com.spmp.user.annotation.OperationLog;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * H5 业主端 — 认证申请 Controller。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/owner/h5/certifications")
@RequiredArgsConstructor
public class H5CertificationController {

    private final CertificationService certificationService;

    /**
     * 提交认证申请。
     */
    @PostMapping
    @OperationLog(module = "业主认证", type = "CREATE", description = "提交认证申请")
    public Result<Void> submitCertification(@Valid @RequestBody H5CertificationCreateDTO createDTO) {
        Long ownerId = SecurityUtils.getCurrentOwnerId();
        certificationService.submitCertification(ownerId, createDTO);
        return Result.success(null);
    }

    /**
     * 我的认证记录。
     */
    @GetMapping
    public Result<List<CertificationVO>> myCertifications() {
        Long ownerId = SecurityUtils.getCurrentOwnerId();
        return Result.success(certificationService.listByOwnerId(ownerId));
    }
}
