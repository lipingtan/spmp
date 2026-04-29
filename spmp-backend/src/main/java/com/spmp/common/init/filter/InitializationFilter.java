package com.spmp.common.init.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spmp.common.init.runtime.RuntimeModeManager;
import com.spmp.common.result.Result;
import com.spmp.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 初始化模式过滤器。
 * <p>
 * 当系统未初始化时，拦截所有非初始化接口请求，返回 HTTP 503。
 * 通过 SecurityConfig 注册到 Spring Security FilterChain 中。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public class InitializationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(InitializationFilter.class);

    private static final List<String> INIT_PATH_PREFIXES = Arrays.asList(
            "/api/v1/init/"
    );

    private static final List<String> STATIC_PATH_PREFIXES = Arrays.asList(
            "/index.html", "/assets/", "/favicon.ico", "/static/"
    );

    private final RuntimeModeManager runtimeModeManager;
    private final ObjectMapper objectMapper;

    public InitializationFilter(RuntimeModeManager runtimeModeManager, ObjectMapper objectMapper) {
        this.runtimeModeManager = runtimeModeManager;
        this.objectMapper = objectMapper;
        log.info("InitializationFilter started, currentMode={}", runtimeModeManager.currentMode());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        if (runtimeModeManager.isBusinessMode()) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        // 初始化接口和静态资源放行
        if (isAllowedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("系统处于初始化模式，拦截请求: {}", path);
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        response.setContentType("application/json;charset=UTF-8");
        String message = "系统处于初始化模式，请先完成初始化配置";
        Result<?> result = Result.fail(ErrorCode.INTERNAL_ERROR, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    private boolean isAllowedPath(String path) {
        for (String prefix : INIT_PATH_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        for (String prefix : STATIC_PATH_PREFIXES) {
            if (path.equals(prefix) || path.startsWith(prefix)) {
                return true;
            }
        }
        // 根路径放行（前端入口）
        if ("/".equals(path)) {
            return true;
        }
        return false;
    }
}
