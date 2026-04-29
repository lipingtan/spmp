package com.spmp.user.service;

import com.spmp.common.security.DataPermissionContext;
import com.spmp.user.domain.dto.MenuTreeDTO;

import java.util.List;
import java.util.Set;

/**
 * 权限缓存服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface PermissionCacheService {

    /**
     * 获取用户权限标识集合（优先 Redis，降级查 DB）。
     */
    Set<String> getUserPermissions(Long userId);

    /**
     * 获取用户数据权限上下文（优先 Redis，降级查 DB）。
     */
    DataPermissionContext getDataPermission(Long userId);

    /**
     * 获取用户菜单树（优先 Redis，降级查 DB）。
     */
    List<MenuTreeDTO> getUserMenuTree(Long userId);

    /**
     * 清除用户权限缓存。
     */
    void clearUserPermissions(Long userId);

    /**
     * 清除用户数据权限缓存。
     */
    void clearUserDataPermission(Long userId);

    /**
     * 清除用户菜单缓存。
     */
    void clearUserMenus(Long userId);

    /**
     * 清除角色下所有用户的缓存。
     */
    void clearRoleUsersCache(Long roleId);
}
