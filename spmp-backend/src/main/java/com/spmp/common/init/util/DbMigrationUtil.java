package com.spmp.common.init.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据库迁移工具类。
 * <p>
 * 扫描 classpath:db/migration/ 下的 SQL 文件，按版本号排序执行。
 * 使用原生 JDBC，不依赖 Spring 容器中的 DataSource。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public class DbMigrationUtil {

    private static final Logger log = LoggerFactory.getLogger(DbMigrationUtil.class);

    private static final String MIGRATION_LOCATION = "classpath:db/migration/*.sql";

    private DbMigrationUtil() {
        // 工具类禁止实例化
    }

    /**
     * 执行数据库迁移。
     * <p>
     * 先通过服务器级别连接创建数据库（如不存在），再切到目标数据库执行迁移脚本。
     *
     * @param serverJdbcUrl 服务器级别 JDBC URL（不带数据库名）
     * @param dbJdbcUrl     目标数据库 JDBC URL（带数据库名）
     * @param databaseName  数据库名称
     * @param username      数据库用户名
     * @param password      数据库密码
     * @return 已执行的脚本文件名列表
     */
    public static List<String> executeMigrations(String serverJdbcUrl, String dbJdbcUrl,
                                                  String databaseName, String username, String password) {
        // 1. 创建数据库（如不存在）
        createDatabaseIfNotExists(serverJdbcUrl, databaseName, username, password);

        // 2. 扫描迁移脚本
        List<Resource> scripts = scanMigrationScripts();
        if (scripts.isEmpty()) {
            log.warn("未找到迁移脚本：{}", MIGRATION_LOCATION);
            return new ArrayList<>();
        }

        // 3. 连接到目标数据库执行迁移
        List<String> executedScripts = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(dbJdbcUrl, username, password)) {
            conn.setAutoCommit(false);

            for (Resource script : scripts) {
                String fileName = script.getFilename();
                log.info("执行迁移脚本：{}", fileName);

                try {
                    String sql = readResource(script);
                    executeScript(conn, sql);
                    conn.commit();
                    executedScripts.add(fileName);
                    log.info("迁移脚本执行成功：{}", fileName);
                } catch (Exception e) {
                    conn.rollback();
                    log.error("迁移脚本执行失败：{}，原因：{}", fileName, e.getMessage(), e);
                    throw new RuntimeException("迁移脚本 " + fileName + " 执行失败: " + e.getMessage(), e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("数据库连接失败: " + e.getMessage(), e);
        }

        return executedScripts;
    }

    /**
     * 创建数据库（如不存在）。
     */
    private static void createDatabaseIfNotExists(String serverJdbcUrl, String databaseName,
                                                   String username, String password) {
        log.info("检查并创建数据库: {}", databaseName);
        try (Connection conn = DriverManager.getConnection(serverJdbcUrl, username, password);
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE DATABASE IF NOT EXISTS `" + databaseName
                    + "` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
            log.info("数据库 {} 已就绪", databaseName);
        } catch (SQLException e) {
            throw new RuntimeException("创建数据库失败: " + e.getMessage(), e);
        }
    }

    /**
     * 扫描迁移脚本并按文件名排序。
     */
    /**
     * 从文件名 {@code V12__xxx.sql} 解析版本序号；不符合约定时排在最后。
     */
    static int extractMigrationVersion(Resource resource) {
        String filename = resource.getFilename();
        if (filename == null || !filename.startsWith("V")) {
            return Integer.MAX_VALUE;
        }
        int sep = filename.indexOf("__");
        if (sep <= 1) {
            return Integer.MAX_VALUE;
        }
        try {
            return Integer.parseInt(filename.substring(1, sep));
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    private static List<Resource> scanMigrationScripts() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(MIGRATION_LOCATION);
            return Arrays.stream(resources)
                    .filter(Resource::isReadable)
                    // 必须按 V{数字}__ 数值排序；纯字符串排序会导致 V10 排在 V2 之前，破坏依赖顺序
                    .sorted(Comparator
                            .comparingInt(DbMigrationUtil::extractMigrationVersion)
                            .thenComparing(Resource::getFilename))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("扫描迁移脚本失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 读取资源文件内容。
     */
    private static String readResource(Resource resource) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    /**
     * 执行 SQL 脚本（支持多条语句，以分号分隔）。
     */
    private static void executeScript(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // 按分号拆分多条 SQL 语句
            String[] statements = sql.split(";");
            for (String s : statements) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        }
    }
}
