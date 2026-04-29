package com.spmp.base.config;

import com.spmp.base.service.BaseCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 基础数据缓存预热 Runner。
 * <p>
 * 系统启动时自动执行缓存预热，加载片区/小区/楼栋基本信息到 Redis。
 * 预热失败时记录 WARN 日志，不影响系统启动。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Component
public class BaseCacheWarmupRunner implements CommandLineRunner {

    private final BaseCacheService baseCacheService;

    public BaseCacheWarmupRunner(BaseCacheService baseCacheService) {
        this.baseCacheService = baseCacheService;
    }

    @Override
    public void run(String... args) {
        try {
            log.info("跳过缓存预热，系统启动中...");
            // 暂时禁用缓存预热，避免启动卡住
            // baseCacheService.warmupCache();
        } catch (Exception e) {
            log.warn("基础数据缓存预热失败，将在首次查询时加载: {}", e.getMessage());
        }
    }
}
