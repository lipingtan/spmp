package com.spmp.common.init.dto;

import lombok.Data;

import java.util.List;

/**
 * 初始化执行结果响应体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class InitResultVO {

    /** 初始化是否成功 */
    private boolean success;

    /** 结果消息 */
    private String message;

    /** 是否已成功切换到业务模式 */
    private boolean modeSwitched;

    /** 初始化摘要信息 */
    private Summary summary;

    /**
     * 初始化摘要。
     */
    @Data
    public static class Summary {

        /** 数据库地址（host:port） */
        private String databaseHost;

        /** 数据库名称 */
        private String databaseName;

        /** Redis 地址（host:port） */
        private String redisHost;

        /** 已执行的迁移脚本数量 */
        private int scriptsExecuted;

        /** 已执行的迁移脚本列表 */
        private List<String> scriptNames;
    }
}
