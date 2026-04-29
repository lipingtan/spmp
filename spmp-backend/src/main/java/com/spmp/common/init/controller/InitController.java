package com.spmp.common.init.controller;

import com.spmp.common.init.dto.ConnectionTestVO;
import com.spmp.common.init.dto.InitConfigDTO;
import com.spmp.common.init.dto.InitResultVO;
import com.spmp.common.init.dto.InitStatusVO;
import com.spmp.common.init.service.InitService;
import com.spmp.common.result.Result;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 部署初始化接口。
 * <p>
 * 提供系统初始化状态检测、连通性测试和初始化执行功能。
 * 所有接口无需认证即可访问。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/init")
public class InitController {

    private final InitService initService;

    public InitController(InitService initService) {
        this.initService = initService;
    }

    /**
     * 检查系统初始化状态。
     *
     * @return 初始化状态
     */
    @GetMapping("/status")
    public Result<InitStatusVO> checkStatus() {
        boolean initialized = initService.isInitialized();
        return Result.success(new InitStatusVO(initialized));
    }

    /**
     * 测试数据库和 Redis 连通性。
     *
     * @param config 初始化配置
     * @return 连通性测试结果
     */
    @PostMapping("/test-connection")
    public Result<ConnectionTestVO> testConnection(@RequestBody @Validated InitConfigDTO config) {
        ConnectionTestVO result = initService.testConnection(config);
        return Result.success(result);
    }

    /**
     * 执行系统初始化。
     *
     * @param config 初始化配置
     * @return 初始化执行结果
     */
    @PostMapping("/execute")
    public Result<InitResultVO> execute(@RequestBody @Validated InitConfigDTO config) {
        InitResultVO result = initService.executeInitialization(config);
        return Result.success(result);
    }
}
