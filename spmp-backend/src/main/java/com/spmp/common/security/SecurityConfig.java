package com.spmp.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spmp.common.init.filter.InitializationFilter;
import com.spmp.common.init.runtime.RuntimeModeManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 安全配置类。
 * <p>
 * 配置 JWT 无状态认证、白名单路径、CSRF 禁用等。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /** 默认白名单路径 */
    private static final String[] DEFAULT_WHITE_LIST = {
            "/api/v1/user/auth/login",
            "/api/v1/user/auth/login/sms",
            "/api/v1/user/auth/sms-code",
            "/api/v1/user/auth/captcha",
            "/api/v1/user/auth/refresh",
            "/api/v1/user/auth/register",
            "/api/v1/owner/h5/register",
            "/api/v1/init/**",
            "/actuator/health",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    @Value("${security.white-list:}")
    private String[] extraWhiteList;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RuntimeModeManager runtimeModeManager;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          RuntimeModeManager runtimeModeManager,
                          ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.runtimeModeManager = runtimeModeManager;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 合并默认白名单和扩展白名单
        List<String> whiteList = new ArrayList<>(Arrays.asList(DEFAULT_WHITE_LIST));
        if (extraWhiteList != null && extraWhiteList.length > 0) {
            for (String path : extraWhiteList) {
                if (path != null && !path.trim().isEmpty()) {
                    whiteList.add(path.trim());
                }
            }
        }
        String[] allWhiteList = whiteList.toArray(new String[0]);

        http
            // 启用 CORS（允许前端跨域请求）
            .cors().and()
            // 禁用 CSRF（JWT 无状态认证）
            .csrf().disable()
            // 无状态会话管理
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers(allWhiteList).permitAll()
                .anyRequest().authenticated();

        http
            // 在 UsernamePasswordAuthenticationFilter 之前注册 JWT 过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // 在 Security FilterChain 最前面注册初始化过滤器
            .addFilterBefore(new InitializationFilter(runtimeModeManager, objectMapper), SecurityContextPersistenceFilter.class);
    }

    /**
     * 密码编码器。
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS 配置。
     * 允许前端开发服务器（localhost:3000/3001）跨域访问。
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
