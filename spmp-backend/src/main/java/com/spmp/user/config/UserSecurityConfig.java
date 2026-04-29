package com.spmp.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

/**
 * user 模块安全扩展配置。
 * <p>
 * 启用方法级权限注解支持。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class UserSecurityConfig {
}
