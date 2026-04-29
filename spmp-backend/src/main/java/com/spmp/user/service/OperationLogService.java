package com.spmp.user.service;

import com.spmp.common.result.PageResult;
import com.spmp.user.domain.dto.OperationLogQueryDTO;
import com.spmp.user.domain.entity.OperationLogDO;

/**
 * 操作日志服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface OperationLogService {

    void saveLog(OperationLogDO logDO);

    PageResult<OperationLogDO> listLogs(OperationLogQueryDTO queryDTO);
}
