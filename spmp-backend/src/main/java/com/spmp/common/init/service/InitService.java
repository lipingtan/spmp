package com.spmp.common.init.service;

import com.spmp.common.init.dto.ConnectionTestVO;
import com.spmp.common.init.dto.InitConfigDTO;
import com.spmp.common.init.dto.InitResultVO;

/**
 * 部署初始化服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface InitService {

    /**
     * 检查系统是否已完成初始化。
     *
     * @return true 表示已初始化
     */
    boolean isInitialized();

    /**
     * 测试数据库和 Redis 的连通性。
     * <p>
     * 使用原生 JDBC 和 Jedis 创建临时连接，不依赖 Spring 容器。
     *
     * @param config 初始化配置
     * @return 连通性测试结果
     */
    ConnectionTestVO testConnection(InitConfigDTO config);

    /**
     * 执行完整初始化流程。
     * <ol>
     *   <li>再次测试连通性</li>
     *   <li>写入 init-config.yml</li>
     *   <li>执行 db/migration/ 下的 SQL 迁移脚本</li>
     *   <li>生成 init.lock 文件</li>
     * </ol>
     *
     * @param config 初始化配置
     * @return 初始化执行结果
     */
    InitResultVO executeInitialization(InitConfigDTO config);
}
