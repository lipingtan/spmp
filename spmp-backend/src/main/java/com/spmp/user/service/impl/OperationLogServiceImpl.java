package com.spmp.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spmp.common.result.PageResult;
import com.spmp.user.domain.dto.OperationLogQueryDTO;
import com.spmp.user.domain.entity.OperationLogDO;
import com.spmp.user.repository.OperationLogMapper;
import com.spmp.user.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 操作日志服务实现。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;

    public OperationLogServiceImpl(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    @Async("operationLogExecutor")
    public void saveLog(OperationLogDO logDO) {
        try {
            operationLogMapper.insert(logDO);
        } catch (Exception e) {
            log.error("保存操作日志失败: {}", e.getMessage());
        }
    }

    @Override
    public PageResult<OperationLogDO> listLogs(OperationLogQueryDTO queryDTO) {
        LambdaQueryWrapper<OperationLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getOperatorName()),
                OperationLogDO::getOperatorName, queryDTO.getOperatorName());
        wrapper.eq(StringUtils.hasText(queryDTO.getModule()),
                OperationLogDO::getModule, queryDTO.getModule());
        wrapper.eq(StringUtils.hasText(queryDTO.getOperationType()),
                OperationLogDO::getOperationType, queryDTO.getOperationType());
        wrapper.ge(queryDTO.getStartTime() != null,
                OperationLogDO::getOperationTime, queryDTO.getStartTime());
        wrapper.le(queryDTO.getEndTime() != null,
                OperationLogDO::getOperationTime, queryDTO.getEndTime());
        wrapper.orderByDesc(OperationLogDO::getOperationTime);

        IPage<OperationLogDO> page = operationLogMapper.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        return PageResult.of(page);
    }
}
