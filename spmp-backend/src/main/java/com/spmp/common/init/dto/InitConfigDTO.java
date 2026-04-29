package com.spmp.common.init.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 初始化配置请求体。
 * <p>
 * 包含数据库和 Redis 的连接配置信息。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class InitConfigDTO {

    @NotNull(message = "数据库配置不能为空")
    @Valid
    private DatabaseConfig database;

    @NotNull(message = "Redis 配置不能为空")
    @Valid
    private RedisConfig redis;

    /**
     * 数据库连接配置。
     */
    @Data
    public static class DatabaseConfig {

        @NotBlank(message = "数据库地址不能为空")
        private String host;

        @NotNull(message = "数据库端口不能为空")
        @Min(value = 1, message = "端口号范围为 1-65535")
        @Max(value = 65535, message = "端口号范围为 1-65535")
        private Integer port = 3306;

        @NotBlank(message = "数据库名称不能为空")
        private String databaseName;

        @NotBlank(message = "数据库用户名不能为空")
        private String username;

        private String password;
    }

    /**
     * Redis 连接配置。
     */
    @Data
    public static class RedisConfig {

        @NotBlank(message = "Redis 地址不能为空")
        private String host;

        @NotNull(message = "Redis 端口不能为空")
        @Min(value = 1, message = "端口号范围为 1-65535")
        @Max(value = 65535, message = "端口号范围为 1-65535")
        private Integer port = 6379;

        private String password;

        @Min(value = 0, message = "数据库编号范围为 0-15")
        @Max(value = 15, message = "数据库编号范围为 0-15")
        private Integer database = 0;
    }
}
