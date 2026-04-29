package com.spmp.common.init.runtime;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 基于运行态的路由数据源。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public class RuntimeRoutingDataSource extends AbstractRoutingDataSource {

    private final RuntimeModeManager runtimeModeManager;

    public RuntimeRoutingDataSource(RuntimeModeManager runtimeModeManager) {
        this.runtimeModeManager = runtimeModeManager;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return runtimeModeManager.currentMode().name();
    }
}
