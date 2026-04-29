package com.spmp.user.controller;

import com.spmp.common.result.PageResult;
import com.spmp.user.domain.dto.OperationLogQueryDTO;
import com.spmp.user.domain.entity.OperationLogDO;
import com.spmp.user.service.OperationLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志控制器。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/user/operation-logs")
public class OperationLogController {

    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @GetMapping
    @PreAuthorize("@perm.check('user:log:list')")
    public PageResult<OperationLogDO> listOperationLogs(OperationLogQueryDTO queryDTO) {
        return operationLogService.listLogs(queryDTO);
    }
}
