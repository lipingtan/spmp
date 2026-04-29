package com.spmp.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.spmp.common.security.DataPermissionInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus 全局配置类。
 * <p>
 * 配置分页插件、乐观锁插件、自动填充处理器。
 * DataPermissionInterceptor 在任务 12 中注册。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Configuration
@MapperScan({"com.spmp.*.repository", "com.spmp.*.*.repository"})
public class MybatisPlusConfig {

    /**
     * 注册 MyBatis Plus 拦截器。
     *
     * @return MybatisPlusInterceptor 实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 数据权限拦截器（需在分页之前执行）
        interceptor.addInnerInterceptor(new DataPermissionInterceptor());
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
}
