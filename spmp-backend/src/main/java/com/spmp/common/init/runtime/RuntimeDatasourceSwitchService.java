package com.spmp.common.init.runtime;

import com.alibaba.druid.pool.DruidDataSource;
import com.spmp.common.exception.BusinessException;
import com.spmp.common.exception.ErrorCode;
import com.spmp.common.init.dto.InitConfigDTO;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 运行时业务数据源切换服务。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Service
public class RuntimeDatasourceSwitchService {

    private final BusinessDatasourceHolder businessDatasourceHolder;
    private final RuntimeModeManager runtimeModeManager;

    public RuntimeDatasourceSwitchService(
            BusinessDatasourceHolder businessDatasourceHolder,
            RuntimeModeManager runtimeModeManager) {
        this.businessDatasourceHolder = businessDatasourceHolder;
        this.runtimeModeManager = runtimeModeManager;
    }

    public void switchToBusiness(InitConfigDTO.DatabaseConfig databaseConfig) {
        DruidDataSource candidate = buildBusinessDataSource(databaseConfig);
        try {
            validate(candidate);
            businessDatasourceHolder.getSwitchableDataSource().replace(candidate);
            runtimeModeManager.switchToBusiness();
        } catch (Exception e) {
            closeQuietly(candidate);
            runtimeModeManager.switchToInit();
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "运行态切换到业务数据源失败: " + e.getMessage());
        }
    }

    public boolean hasBusinessDatasource() {
        return businessDatasourceHolder.getSwitchableDataSource().hasDelegate();
    }

    private DruidDataSource buildBusinessDataSource(InitConfigDTO.DatabaseConfig databaseConfig) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(String.format(
                "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&connectTimeout=10000&socketTimeout=10000",
                databaseConfig.getHost(), databaseConfig.getPort(), databaseConfig.getDatabaseName()));
        dataSource.setUsername(databaseConfig.getUsername());
        dataSource.setPassword(databaseConfig.getPassword());
        dataSource.setInitialSize(1);
        dataSource.setMinIdle(1);
        dataSource.setMaxActive(20);
        dataSource.setMaxWait(10000);
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(true);
        dataSource.setTestOnReturn(false);
        return dataSource;
    }

    private void validate(DataSource dataSource) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            if (!conn.isValid(10)) {
                throw new IllegalStateException("业务数据源连接校验失败");
            }
        }
    }

    private void closeQuietly(DruidDataSource dataSource) {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
