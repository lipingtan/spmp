package com.spmp.notice.support;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 公告模块上下文工具。
 * <p>
 * 从 SecurityContext 中提取当前登录用户的 userId/username/ownerId。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public final class NoticeSecurityUtils {

    private NoticeSecurityUtils() {
    }

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

    public static String getCurrentUsername() {
        Claims claims = getClaims();
        if (claims == null) {
            return null;
        }
        Object username = claims.get("username");
        return username == null ? null : username.toString();
    }

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

    private static Claims getClaims() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object details = authentication.getDetails();
        if (details instanceof Claims) {
            return (Claims) details;
        }
        return null;
    }
}
