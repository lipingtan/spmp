package com.spmp.user.controller;

import com.spmp.common.result.Result;
import com.spmp.user.domain.vo.DataPermissionOptionVO;
import com.spmp.user.service.DataPermissionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据权限控制器。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/user/data-permission")
public class DataPermissionController {

    private final DataPermissionService dataPermissionService;

    public DataPermissionController(DataPermissionService dataPermissionService) {
        this.dataPermissionService = dataPermissionService;
    }

    @GetMapping("/options")
    @PreAuthorize("@perm.check('user:role:assign')")
    public Result<DataPermissionOptionVO> getOptions() {
        return Result.success(dataPermissionService.getOptions());
    }
}
