package com.spmp.user.controller;

import com.spmp.common.result.PageResult;
import com.spmp.user.domain.dto.LoginLogQueryDTO;
import com.spmp.user.domain.entity.LoginLogDO;
import com.spmp.user.service.LoginLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录日志控制器。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/user/login-logs")
public class LoginLogController {

    private final LoginLogService loginLogService;

    public LoginLogController(LoginLogService loginLogService) {
        this.loginLogService = loginLogService;
    }

    @GetMapping
    @PreAuthorize("@perm.check('user:log:list')")
    public PageResult<LoginLogDO> listLoginLogs(LoginLogQueryDTO queryDTO) {
        return loginLogService.listLoginLogs(queryDTO);
    }
}
