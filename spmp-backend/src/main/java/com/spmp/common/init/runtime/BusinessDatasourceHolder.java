package com.spmp.common.init.runtime;

/**
 * 业务数据源持有器。
 * <p>
 * 作为普通 Spring Bean 存在，但自身不实现 {@code DataSource}，避免被 Spring Boot 当成待初始化数据源参与循环依赖。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public class BusinessDatasourceHolder {

    private final SwitchableDataSource switchableDataSource;

    public BusinessDatasourceHolder(SwitchableDataSource switchableDataSource) {
        this.switchableDataSource = switchableDataSource;
    }

    public SwitchableDataSource getSwitchableDataSource() {
        return switchableDataSource;
    }
}
