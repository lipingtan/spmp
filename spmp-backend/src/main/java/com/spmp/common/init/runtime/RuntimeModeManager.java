package com.spmp.common.init.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 运行态管理器。
 * <p>
 * 统一管理初始化模式与业务模式，供过滤器、安全链、路由数据源读取。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Component
public class RuntimeModeManager {

    private static final Logger log = LoggerFactory.getLogger(RuntimeModeManager.class);

    private static final String INIT_LOCK_PATH = "config" + File.separator + "init.lock";

    private final AtomicReference<RuntimeMode> currentMode =
            new AtomicReference<>(new File(INIT_LOCK_PATH).exists() ? RuntimeMode.BUSINESS : RuntimeMode.INIT);

    public RuntimeMode currentMode() {
        return currentMode.get();
    }

    public boolean isInitMode() {
        return currentMode() == RuntimeMode.INIT;
    }

    public boolean isBusinessMode() {
        return currentMode() == RuntimeMode.BUSINESS;
    }

    public void switchToBusiness() {
        currentMode.set(RuntimeMode.BUSINESS);
        log.info("runtime mode switched to BUSINESS");
    }

    public void switchToInit() {
        currentMode.set(RuntimeMode.INIT);
        log.info("runtime mode switched to INIT");
    }
}
