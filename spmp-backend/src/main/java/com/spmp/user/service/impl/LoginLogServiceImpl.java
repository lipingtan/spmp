package com.spmp.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spmp.common.result.PageResult;
import com.spmp.user.domain.dto.LoginLogQueryDTO;
import com.spmp.user.domain.entity.LoginLogDO;
import com.spmp.user.repository.LoginLogMapper;
import com.spmp.user.service.LoginLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 登录日志服务实现。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class LoginLogServiceImpl implements LoginLogService {

    private final LoginLogMapper loginLogMapper;

    public LoginLogServiceImpl(LoginLogMapper loginLogMapper) {
        this.loginLogMapper = loginLogMapper;
    }

    @Override
    @Async("operationLogExecutor")
    public void saveLoginLog(LoginLogDO logDO) {
        try {
            loginLogMapper.insert(logDO);
        } catch (Exception e) {
            log.error("保存登录日志失败: {}", e.getMessage());
        }
    }

    @Override
    public PageResult<LoginLogDO> listLoginLogs(LoginLogQueryDTO queryDTO) {
        LambdaQueryWrapper<LoginLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getUsername()),
                LoginLogDO::getUsername, queryDTO.getUsername());
        wrapper.like(StringUtils.hasText(queryDTO.getLoginIp()),
                LoginLogDO::getLoginIp, queryDTO.getLoginIp());
        wrapper.eq(queryDTO.getLoginResult() != null,
                LoginLogDO::getLoginResult, queryDTO.getLoginResult());
        wrapper.ge(queryDTO.getStartTime() != null,
                LoginLogDO::getLoginTime, queryDTO.getStartTime());
        wrapper.le(queryDTO.getEndTime() != null,
                LoginLogDO::getLoginTime, queryDTO.getEndTime());
        wrapper.orderByDesc(LoginLogDO::getLoginTime);

        IPage<LoginLogDO> page = loginLogMapper.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        return PageResult.of(page);
    }
}
