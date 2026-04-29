package com.spmp.user.controller;

import com.spmp.common.result.PageResult;
import com.spmp.common.result.Result;
import com.spmp.user.annotation.OperationLog;
import com.spmp.user.domain.dto.UserCreateDTO;
import com.spmp.user.domain.dto.UserPageDTO;
import com.spmp.user.domain.dto.UserQueryDTO;
import com.spmp.user.domain.dto.UserUpdateDTO;
import com.spmp.user.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 用户管理控制器。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/user/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("@perm.check('user:user:list')")
    public PageResult<UserPageDTO> listUsers(UserQueryDTO queryDTO) {
        return userService.listUsers(queryDTO);
    }

    @PostMapping
    @PreAuthorize("@perm.check('user:user:create')")
    @OperationLog(module = "用户管理", type = "CREATE", description = "新增用户")
    public Result<Void> createUser(@Valid @RequestBody UserCreateDTO createDTO) {
        userService.createUser(createDTO);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.check('user:user:edit')")
    @OperationLog(module = "用户管理", type = "UPDATE", description = "编辑用户")
    public Result<Void> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO updateDTO) {
        userService.updateUser(id, updateDTO);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.check('user:user:delete')")
    @OperationLog(module = "用户管理", type = "DELETE", description = "删除用户")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success(null);
    }

    @DeleteMapping("/batch")
    @PreAuthorize("@perm.check('user:user:delete')")
    @OperationLog(module = "用户管理", type = "DELETE", description = "批量删除用户")
    public Result<Void> batchDeleteUsers(@RequestBody List<Long> ids) {
        userService.batchDeleteUsers(ids);
        return Result.success(null);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@perm.check('user:user:edit')")
    @OperationLog(module = "用户管理", type = "UPDATE", description = "用户状态切换")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success(null);
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("@perm.check('user:user:reset-pwd')")
    @OperationLog(module = "用户管理", type = "UPDATE", description = "重置密码")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success(null);
    }

    @PutMapping("/batch-status")
    @PreAuthorize("@perm.check('user:user:edit')")
    @OperationLog(module = "用户管理", type = "UPDATE", description = "批量状态切换")
    public Result<Void> batchUpdateStatus(@RequestBody List<Long> ids, @RequestParam Integer status) {
        userService.batchUpdateStatus(ids, status);
        return Result.success(null);
    }
}
