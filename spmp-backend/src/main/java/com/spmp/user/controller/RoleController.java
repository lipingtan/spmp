package com.spmp.user.controller;

import com.spmp.common.result.PageResult;
import com.spmp.common.result.Result;
import com.spmp.user.annotation.OperationLog;
import com.spmp.user.domain.dto.*;
import com.spmp.user.domain.vo.RoleSimpleVO;
import com.spmp.user.service.RoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 角色管理控制器。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/user/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("@perm.check('user:role:list')")
    public PageResult<RolePageDTO> listRoles(RoleQueryDTO queryDTO) {
        return roleService.listRoles(queryDTO);
    }

    @GetMapping("/list")
    @PreAuthorize("@perm.check('user:role:list')")
    public Result<List<RoleSimpleVO>> listAllRoles() {
        return Result.success(roleService.listAllRoles());
    }

    @PostMapping
    @PreAuthorize("@perm.check('user:role:create')")
    @OperationLog(module = "角色管理", type = "CREATE", description = "新增角色")
    public Result<Void> createRole(@Valid @RequestBody RoleCreateDTO createDTO) {
        roleService.createRole(createDTO);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.check('user:role:edit')")
    @OperationLog(module = "角色管理", type = "UPDATE", description = "编辑角色")
    public Result<Void> updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateDTO updateDTO) {
        roleService.updateRole(id, updateDTO);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.check('user:role:delete')")
    @OperationLog(module = "角色管理", type = "DELETE", description = "删除角色")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success(null);
    }

    @DeleteMapping("/batch")
    @PreAuthorize("@perm.check('user:role:delete')")
    @OperationLog(module = "角色管理", type = "DELETE", description = "批量删除角色")
    public Result<Void> batchDeleteRoles(@RequestBody List<Long> ids) {
        roleService.batchDeleteRoles(ids);
        return Result.success(null);
    }

    @GetMapping("/{id}/menus")
    @PreAuthorize("@perm.check('user:role:list')")
    public Result<List<Long>> getRoleMenuIds(@PathVariable Long id) {
        return Result.success(roleService.getRoleMenuIds(id));
    }

    @PutMapping("/{id}/menus")
    @PreAuthorize("@perm.check('user:role:assign')")
    @OperationLog(module = "角色管理", type = "UPDATE", description = "分配菜单权限")
    public Result<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(id, menuIds);
        return Result.success(null);
    }

    @PutMapping("/{id}/data-permission")
    @PreAuthorize("@perm.check('user:role:assign')")
    @OperationLog(module = "角色管理", type = "UPDATE", description = "配置数据权限")
    public Result<Void> configDataPermission(@PathVariable Long id,
                                             @Valid @RequestBody DataPermissionConfigDTO configDTO) {
        roleService.configDataPermission(id, configDTO);
        return Result.success(null);
    }
}
