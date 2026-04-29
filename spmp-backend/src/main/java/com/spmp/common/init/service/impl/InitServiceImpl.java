package com.spmp.common.init.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.spmp.common.exception.BusinessException;
import com.spmp.common.exception.ErrorCode;
import com.spmp.common.init.dto.ConnectionTestVO;
import com.spmp.common.init.dto.InitConfigDTO;
import com.spmp.common.init.dto.InitResultVO;
import com.spmp.common.init.runtime.RuntimeDatasourceSwitchService;
import com.spmp.common.init.service.InitService;
import com.spmp.common.init.util.AesEncryptUtil;
import com.spmp.common.init.util.DbMigrationUtil;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 部署初始化服务实现。
 * <p>
 * 使用原生 JDBC 和 Lettuce 进行连通性测试和数据库迁移，
 * 不依赖 Spring 容器中的 DataSource 和 RedisTemplate。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Service
public class InitServiceImpl implements InitService {

    private static final Logger log = LoggerFactory.getLogger(InitServiceImpl.class);

    private static final String CONFIG_DIR = "config";
    private static final String INIT_LOCK_FILE = CONFIG_DIR + File.separator + "init.lock";
    private static final String INIT_CONFIG_FILE = CONFIG_DIR + File.separator + "init-config.yml";
    private static final String INIT_CONFIG_BACKUP = CONFIG_DIR + File.separator + "init-config.yml.bak";

    /** 连接测试超时时间（秒） */
    private static final int CONNECTION_TIMEOUT_SECONDS = 10;

    /**
     * 连通性检测使用的系统库（固定为 MySQL 内置 {@code mysql} 库，不依赖业务库如 {@code spmp} 是否已创建）。
     */
    private static final String CONNECTIVITY_TEST_CATALOG = "mysql";

    private final RuntimeDatasourceSwitchService runtimeDatasourceSwitchService;

    public InitServiceImpl(RuntimeDatasourceSwitchService runtimeDatasourceSwitchService) {
        this.runtimeDatasourceSwitchService = runtimeDatasourceSwitchService;
    }

    @Override
    public boolean isInitialized() {
        return new File(INIT_LOCK_FILE).exists();
    }

    @Override
    public ConnectionTestVO testConnection(InitConfigDTO config) {
        ConnectionTestVO result = new ConnectionTestVO();

        // 测试数据库连接
        testDatabaseConnection(config.getDatabase(), result);

        // 测试 Redis 连接
        testRedisConnection(config.getRedis(), result);

        return result;
    }


    @Override
    public InitResultVO executeInitialization(InitConfigDTO config) {
        // 防重复执行
        if (isInitialized()) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "系统已完成初始化，不可重复执行");
        }

        // 1. 再次测试连通性
        ConnectionTestVO testResult = testConnection(config);
        if (!testResult.isDatabaseConnected()) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "数据库连接失败: " + testResult.getDatabaseMessage());
        }
        if (!testResult.isRedisConnected()) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "Redis 连接失败: " + testResult.getRedisMessage());
        }

        // 2. 写入 init-config.yml
        writeInitConfig(config);

        // 3. 创建数据库（如不存在）并执行迁移脚本
        String serverJdbcUrl = buildServerJdbcUrl(config.getDatabase());
        String dbJdbcUrl = buildJdbcUrl(config.getDatabase());
        List<String> executedScripts = DbMigrationUtil.executeMigrations(
                serverJdbcUrl, dbJdbcUrl, config.getDatabase().getDatabaseName(),
                config.getDatabase().getUsername(), config.getDatabase().getPassword());

        // 4. 生成 init.lock
        writeInitLock(config, executedScripts);

        // 5. 运行时切换到业务数据源，无需重启
        runtimeDatasourceSwitchService.switchToBusiness(config.getDatabase());

        // 6. 构建返回结果
        InitResultVO result = new InitResultVO();
        result.setSuccess(true);
        result.setModeSwitched(true);
        result.setMessage("初始化完成，已切换到业务模式");

        InitResultVO.Summary summary = new InitResultVO.Summary();
        summary.setDatabaseHost(config.getDatabase().getHost() + ":" + config.getDatabase().getPort());
        summary.setDatabaseName(config.getDatabase().getDatabaseName());
        summary.setRedisHost(config.getRedis().getHost() + ":" + config.getRedis().getPort());
        summary.setScriptsExecuted(executedScripts.size());
        summary.setScriptNames(executedScripts);
        result.setSummary(summary);

        log.info("系统初始化完成并切换到业务模式，数据库: {}:{}/{}，Redis: {}:{}，执行脚本数: {}",
                config.getDatabase().getHost(), config.getDatabase().getPort(),
                config.getDatabase().getDatabaseName(),
                config.getRedis().getHost(), config.getRedis().getPort(),
                executedScripts.size());

        return result;
    }

    /**
     * 测试数据库连通性（原生 JDBC，服务器级别连接，不要求数据库已存在）。
     */
    private void testDatabaseConnection(InitConfigDTO.DatabaseConfig dbConfig, ConnectionTestVO result) {
        String jdbcUrl = buildJdbcUrlForConnectivityTest(dbConfig);
        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbConfig.getUsername(), dbConfig.getPassword())) {
            if (conn.isValid(CONNECTION_TIMEOUT_SECONDS)) {
                result.setDatabaseConnected(true);
                result.setDatabaseMessage("连接成功");
            } else {
                result.setDatabaseConnected(false);
                result.setDatabaseMessage("连接超时");
            }
        } catch (Exception e) {
            log.warn("数据库连接测试失败: {}", e.getMessage());
            result.setDatabaseConnected(false);
            result.setDatabaseMessage(parseDatabaseError(e));
        }
    }

    /**
     * 测试 Redis 连通性（Lettuce）。
     */
    private void testRedisConnection(InitConfigDTO.RedisConfig redisConfig, ConnectionTestVO result) {
        RedisURI.Builder uriBuilder = RedisURI.builder()
                .withHost(redisConfig.getHost())
                .withPort(redisConfig.getPort())
                .withDatabase(redisConfig.getDatabase())
                .withTimeout(Duration.ofSeconds(CONNECTION_TIMEOUT_SECONDS));

        if (redisConfig.getPassword() != null && !redisConfig.getPassword().isEmpty()) {
            uriBuilder.withPassword(redisConfig.getPassword().toCharArray());
        }

        RedisClient client = RedisClient.create(uriBuilder.build());
        try (StatefulRedisConnection<String, String> conn = client.connect()) {
            String pong = conn.sync().ping();
            if ("PONG".equals(pong)) {
                result.setRedisConnected(true);
                result.setRedisMessage("连接成功");
            } else {
                result.setRedisConnected(false);
                result.setRedisMessage("Redis 响应异常: " + pong);
            }
        } catch (Exception e) {
            log.warn("Redis 连接测试失败: {}", e.getMessage());
            result.setRedisConnected(false);
            result.setRedisMessage("连接失败: " + e.getMessage());
        } finally {
            client.shutdown();
        }
    }

    /**
     * 构建 JDBC URL（带数据库名，用于迁移脚本执行）。
     */
    private String buildJdbcUrl(InitConfigDTO.DatabaseConfig dbConfig) {
        return String.format(
                "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&connectTimeout=%d&socketTimeout=%d",
                dbConfig.getHost(), dbConfig.getPort(), dbConfig.getDatabaseName(),
                CONNECTION_TIMEOUT_SECONDS * 1000, CONNECTION_TIMEOUT_SECONDS * 1000);
    }

    /**
     * 构建服务器级别 JDBC URL（不带数据库名，用于连通性测试和创建数据库）。
     */
    private String buildServerJdbcUrl(InitConfigDTO.DatabaseConfig dbConfig) {
        return String.format(
                "jdbc:mysql://%s:%d?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&connectTimeout=%d&socketTimeout=%d",
                dbConfig.getHost(), dbConfig.getPort(),
                CONNECTION_TIMEOUT_SECONDS * 1000, CONNECTION_TIMEOUT_SECONDS * 1000);
    }

    /**
     * 构建用于「连通性检测」的 JDBC URL：显式连到 {@link #CONNECTIVITY_TEST_CATALOG}，避免依赖目标业务库是否已存在。
     */
    private String buildJdbcUrlForConnectivityTest(InitConfigDTO.DatabaseConfig dbConfig) {
        return String.format(
                "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&connectTimeout=%d&socketTimeout=%d",
                dbConfig.getHost(), dbConfig.getPort(), CONNECTIVITY_TEST_CATALOG,
                CONNECTION_TIMEOUT_SECONDS * 1000, CONNECTION_TIMEOUT_SECONDS * 1000);
    }

    /**
     * 解析数据库连接错误信息。
     */
    private String parseDatabaseError(Exception e) {
        String msg = e.getMessage();
        if (msg == null) {
            return "未知错误";
        }
        if (msg.contains("Communications link failure") || msg.contains("connect timed out")) {
            return "连接超时，请检查数据库地址和端口";
        }
        if (msg.contains("Access denied")) {
            return "认证失败，请检查用户名和密码";
        }
        if (msg.contains("Unknown database")) {
            return "数据库不存在，请检查数据库名称";
        }
        return "连接失败: " + msg;
    }

    /**
     * 写入 init-config.yml 配置文件。
     */
    private void writeInitConfig(InitConfigDTO config) {
        ensureConfigDir();
        File configFile = new File(INIT_CONFIG_FILE);

        // 备份已有配置
        if (configFile.exists()) {
            try {
                Files.copy(configFile.toPath(), new File(INIT_CONFIG_BACKUP).toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                log.warn("备份配置文件失败: {}", e.getMessage());
            }
        }

        // 构建 YAML 内容
        Map<String, Object> springConfig = new LinkedHashMap<>();
        Map<String, Object> datasource = new LinkedHashMap<>();
        datasource.put("url", buildJdbcUrl(config.getDatabase()));
        datasource.put("username", config.getDatabase().getUsername());
        datasource.put("password", AesEncryptUtil.wrapEncrypt(config.getDatabase().getPassword()));
        datasource.put("driver-class-name", "com.mysql.cj.jdbc.Driver");

        Map<String, Object> redis = new LinkedHashMap<>();
        redis.put("host", config.getRedis().getHost());
        redis.put("port", config.getRedis().getPort());
        redis.put("password", AesEncryptUtil.wrapEncrypt(config.getRedis().getPassword()));
        redis.put("database", config.getRedis().getDatabase());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("redis", redis);

        Map<String, Object> spring = new LinkedHashMap<>();
        spring.put("datasource", datasource);
        spring.put("data", data);
        springConfig.put("spring", spring);

        // 写入文件
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);

        try (FileWriter writer = new FileWriter(configFile)) {
            yaml.dump(springConfig, writer);
            log.info("配置文件写入成功: {}", INIT_CONFIG_FILE);
        } catch (IOException e) {
            // 回滚
            File backup = new File(INIT_CONFIG_BACKUP);
            if (backup.exists()) {
                try {
                    Files.copy(backup.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    log.error("配置文件回滚失败", ex);
                }
            }
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "配置文件写入失败: " + e.getMessage());
        }
    }

    /**
     * 生成 init.lock 文件。
     */
    private void writeInitLock(InitConfigDTO config, List<String> executedScripts) {
        ensureConfigDir();
        try {
            Map<String, Object> lockData = new LinkedHashMap<>();
            lockData.put("initialized", true);
            lockData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            lockData.put("databaseHost", config.getDatabase().getHost() + ":" + config.getDatabase().getPort());
            lockData.put("databaseName", config.getDatabase().getDatabaseName());
            lockData.put("redisHost", config.getRedis().getHost() + ":" + config.getRedis().getPort());
            lockData.put("scriptsExecuted", executedScripts);

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(new File(INIT_LOCK_FILE), lockData);
            log.info("初始化锁定文件生成成功: {}", INIT_LOCK_FILE);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "初始化锁定文件生成失败: " + e.getMessage());
        }
    }

    /**
     * 确保 config 目录存在。
     */
    private void ensureConfigDir() {
        File dir = new File(CONFIG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}