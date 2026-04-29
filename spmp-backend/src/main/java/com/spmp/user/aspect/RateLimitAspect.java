package com.spmp.user.aspect;

import com.spmp.common.exception.BusinessException;
import com.spmp.common.exception.ErrorCode;
import com.spmp.common.util.RedisUtils;
import com.spmp.user.annotation.RateLimit;
import com.spmp.user.constant.UserErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * 限流切面。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    private final RedisUtils redisUtils;

    public RateLimitAspect(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    @Before("@annotation(rateLimit)")
    public void before(JoinPoint joinPoint, RateLimit rateLimit) {
        String dimensionValue = getDimensionValue(rateLimit.dimension());
        String redisKey = "rate-limit:" + rateLimit.key() + ":" + dimensionValue;

        try {
            Long count = redisUtils.increment(redisKey, 1);
            if (count != null && count == 1) {
                redisUtils.expire(redisKey, rateLimit.window(), TimeUnit.SECONDS);
            }
            if (count != null && count > rateLimit.maxCount()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST,
                        UserErrorCode.RATE_LIMIT_EXCEEDED.getMessage());
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            // Redis 不可用时降级放行
            log.warn("限流检查失败（Redis 不可用），降级放行, key={}", redisKey);
        }
    }

    private String getDimensionValue(String dimension) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return "unknown";
        }
        HttpServletRequest request = attrs.getRequest();

        switch (dimension.toUpperCase()) {
            case "IP":
                return getClientIp(request);
            case "USER":
                return request.getRemoteUser() != null ? request.getRemoteUser() : getClientIp(request);
            default:
                return getClientIp(request);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
