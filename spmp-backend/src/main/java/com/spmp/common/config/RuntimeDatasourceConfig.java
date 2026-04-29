package com.spmp.common.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.spmp.common.init.runtime.BusinessDatasourceHolder;
import com.spmp.common.init.runtime.RuntimeMode;
import com.spmp.common.init.runtime.RuntimeModeManager;
import com.spmp.common.init.runtime.RuntimeRoutingDataSource;
import com.spmp.common.init.runtime.SwitchableDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 运行时双数据源配置。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Configuration
public class RuntimeDatasourceConfig {

    private static final String PLACEHOLDER_CATALOG = "mysql";

    @Bean
    public BusinessDatasourceHolder businessDatasourceHolder(Environment environment, RuntimeModeManager runtimeModeManager) {
        DataSource initialDelegate = null;
        if (runtimeModeManager.currentMode() == RuntimeMode.BUSINESS) {
            initialDelegate = buildDruidDataSource(
                    environment.getProperty("spring.datasource.url"),
                    environment.getProperty("spring.datasource.username"),
                    environment.getProperty("spring.datasource.password"),
                    1,
                    1);
        }
        return new BusinessDatasourceHolder(new SwitchableDataSource(initialDelegate));
    }

    @Primary
    @Bean
    public DataSource dataSource(RuntimeModeManager runtimeModeManager,
                                 Environment environment,
                                 BusinessDatasourceHolder businessDatasourceHolder) {
        DataSource placeholderDataSource = buildDruidDataSource(
                replaceJdbcCatalog(environment.getProperty("spring.datasource.url"), PLACEHOLDER_CATALOG),
                environment.getProperty("spring.datasource.username"),
                environment.getProperty("spring.datasource.password"),
                0,
                0);
        SwitchableDataSource businessSwitchableDataSource = businessDatasourceHolder.getSwitchableDataSource();
        RuntimeRoutingDataSource routingDataSource = new RuntimeRoutingDataSource(runtimeModeManager);
        Map<Object, Object> targetDataSources = new HashMap<>(4);
        targetDataSources.put(RuntimeMode.INIT.name(), placeholderDataSource);
        targetDataSources.put(RuntimeMode.BUSINESS.name(), businessSwitchableDataSource);
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(placeholderDataSource);
        routingDataSource.afterPropertiesSet();
        return routingDataSource;
    }

    private DruidDataSource buildDruidDataSource(String url, String username, String password, int initialSize, int minIdle) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setInitialSize(initialSize);
        dataSource.setMinIdle(minIdle);
        dataSource.setMaxActive(20);
        dataSource.setMaxWait(10000);
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(true);
        dataSource.setTestOnReturn(false);
        return dataSource;
    }

    private String replaceJdbcCatalog(String jdbcUrl, String newCatalog) {
        if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:mysql://")) {
            return jdbcUrl;
        }
        int queryIdx = jdbcUrl.indexOf('?');
        String withoutQuery = queryIdx >= 0 ? jdbcUrl.substring(0, queryIdx) : jdbcUrl;
        String query = queryIdx >= 0 ? jdbcUrl.substring(queryIdx) : "";
        int slashIdx = withoutQuery.indexOf('/', "jdbc:mysql://".length());
        if (slashIdx < 0) {
            return withoutQuery + "/" + newCatalog + query;
        }
        return withoutQuery.substring(0, slashIdx) + "/" + newCatalog + query;
    }
}
