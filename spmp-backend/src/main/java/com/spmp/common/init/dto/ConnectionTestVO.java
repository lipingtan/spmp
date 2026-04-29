package com.spmp.common.init.dto;

import lombok.Data;

/**
 * 连通性测试响应体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class ConnectionTestVO {

    /** 数据库是否连接成功 */
    private boolean databaseConnected;

    /** 数据库连接测试消息 */
    private String databaseMessage;

    /** Redis 是否连接成功 */
    private boolean redisConnected;

    /** Redis 连接测试消息 */
    private String redisMessage;
}
