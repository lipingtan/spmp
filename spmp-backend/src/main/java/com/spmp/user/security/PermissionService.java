package com.spmp.user.security;

import com.spmp.user.constant.UserConstants;
import com.spmp.user.service.PermissionCacheService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 权限校验 Bean，供 @PreAuthorize("@perm.check('xxx')") 使用。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Component("perm")
public class PermissionService {

    private final PermissionCacheService permissionCacheService;

    public PermissionService(PermissionCacheService permissionCacheService) {
        this.permissionCacheService = permissionCacheService;
    }

    /**
     * 校验当前用户是否拥有指定权限标识。
     *
     * @param permissionCode 权限标识
     * @return true 表示有权限
     */
    public boolean check(String permissionCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // 获取用户 ID
        Long userId = getUserIdFromAuth(authentication);
        if (userId == null) {
            return false;
        }

        // 超级管理员直接放行
        if (UserConstants.SUPER_ADMIN_USER_ID.equals(userId)) {
            return true;
        }

        // 从缓存获取权限列表
        Set<String> permissions = permissionCacheService.getUserPermissions(userId);
        return permissions != null && permissions.contains(permissionCode);
    }

    /**
     * 从 Authentication 中获取用户 ID。
     */
    private Long getUserIdFromAuth(Authentication authentication) {
        Object details = authentication.getDetails();
        if (details instanceof Claims) {
            Claims claims = (Claims) details;
            Object userIdObj = claims.get("userId");
            if (userIdObj instanceof Number) {
                return ((Number) userIdObj).longValue();
            }
        }
        return null;
    }
}
