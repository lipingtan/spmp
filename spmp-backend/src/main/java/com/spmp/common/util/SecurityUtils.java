package com.spmp.common.util;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具类。
 * <p>
 * 从 SecurityContext 中提取当前登录用户信息（userId、ownerId 等）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // 工具类禁止实例化
    }

    /**
     * 获取当前登录用户ID。
     *
     * @return 用户ID，未登录时返回 null
     */
    public static Long getCurrentUserId() {
        Claims claims = getClaims();
        if (claims == null) {
            return null;
        }
        Object userId = claims.get("userId");
        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }
        return null;
    }

    /**
     * 获取当前登录业主ID（H5 端使用）。
     *
     * @return 业主ID，未登录或非业主时返回 null
     */
    public static Long getCurrentOwnerId() {
        Claims claims = getClaims();
        if (claims == null) {
            return null;
        }
        Object ownerId = claims.get("ownerId");
        if (ownerId instanceof Number) {
            return ((Number) ownerId).longValue();
        }
        return null;
    }

    /**
     * 获取当前登录用户名。
     *
     * @return 用户名，未登录时返回 null
     */
    public static String getCurrentUsername() {
        Claims claims = getClaims();
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 从 SecurityContext 获取 JWT Claims。
     *
     * @return Claims 对象，未登录时返回 null
     */
    private static Claims getClaims() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getDetails() instanceof Claims) {
                return (Claims) authentication.getDetails();
            }
        } catch (Exception e) {
            // 忽略异常，返回 null
        }
        return null;
    }
}
