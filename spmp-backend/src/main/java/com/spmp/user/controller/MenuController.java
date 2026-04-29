package com.spmp.user.controller;

import com.spmp.common.result.Result;
import com.spmp.user.annotation.OperationLog;
import com.spmp.user.domain.dto.MenuCreateDTO;
import com.spmp.user.domain.dto.MenuTreeDTO;
import com.spmp.user.domain.dto.MenuUpdateDTO;
import com.spmp.user.service.MenuService;
import io.jsonwebtoken.Claims;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 菜单管理控制器。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/user/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/tree")
    @PreAuthorize("@perm.check('user:menu:list')")
    public Result<List<MenuTreeDTO>> getMenuTree(
            @RequestParam(required = false) String menuName,
            @RequestParam(required = false) Integer status) {
        return Result.success(menuService.getMenuTree(menuName, status));
    }

    @GetMapping("/user-tree")
    public Result<List<MenuTreeDTO>> getUserMenuTree() {
        Long userId = getCurrentUserId();
        return Result.success(menuService.getUserMenuTree(userId));
    }

    @PostMapping
    @PreAuthorize("@perm.check('user:menu:create')")
    @OperationLog(module = "菜单管理", type = "CREATE", description = "新增菜单")
    public Result<Void> createMenu(@Valid @RequestBody MenuCreateDTO createDTO) {
        menuService.createMenu(createDTO);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.check('user:menu:edit')")
    @OperationLog(module = "菜单管理", type = "UPDATE", description = "编辑菜单")
    public Result<Void> updateMenu(@PathVariable Long id, @Valid @RequestBody MenuUpdateDTO updateDTO) {
        menuService.updateMenu(id, updateDTO);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.check('user:menu:delete')")
    @OperationLog(module = "菜单管理", type = "DELETE", description = "删除菜单")
    public Result<Void> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return Result.success(null);
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof Claims) {
            Claims claims = (Claims) auth.getDetails();
            Object userId = claims.get("userId");
            if (userId instanceof Number) {
                return ((Number) userId).longValue();
            }
        }
        return null;
    }
}
