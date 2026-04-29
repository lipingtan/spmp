package com.spmp.common.security;

import com.spmp.common.util.RedisUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 认证过滤器。
 * <p>
 * 从 Authorization 头提取 Bearer token，验证后设置 SecurityContext。
 * 支持 Token 黑名单校验（Redis token:blacklist:{jti}）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisUtils redisUtils;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, RedisUtils redisUtils) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisUtils = redisUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = resolveToken(request);
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                Claims claims = jwtTokenProvider.parseToken(token);

                // 校验 Token 黑名单
                if (isTokenBlacklisted(claims.getId())) {
                    log.debug("Token 已在黑名单中, jti={}", claims.getId());
                    filterChain.doFilter(request, response);
                    return;
                }

                String username = claims.getSubject();

                // 提取角色列表
                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);
                List<SimpleGrantedAuthority> authorities = roles != null
                        ? roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                        : Collections.emptyList();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                // 将 Claims 存入 details，便于后续获取 userId 等信息
                authentication.setDetails(claims);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.debug("JWT 认证失败: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 校验 Token 是否在黑名单中。
     * Redis 不可用时降级放行。
     *
     * @param jti Token 唯一标识
     * @return true 表示在黑名单中
     */
    private boolean isTokenBlacklisted(String jti) {
        if (jti == null || jti.isEmpty()) {
            return false;
        }
        try {
            Boolean exists = redisUtils.hasKey(TOKEN_BLACKLIST_PREFIX + jti);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            // Redis 不可用时降级放行
            log.warn("Token 黑名单校验失败（Redis 不可用），降级放行, jti={}", jti);
            return false;
        }
    }

    /**
     * 从请求头提取 Bearer token。
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
