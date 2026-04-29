package com.spmp.user.service;

import com.spmp.common.result.PageResult;
import com.spmp.user.domain.dto.LoginLogQueryDTO;
import com.spmp.user.domain.entity.LoginLogDO;

/**
 * 登录日志服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface LoginLogService {

    void saveLoginLog(LoginLogDO logDO);

    PageResult<LoginLogDO> listLoginLogs(LoginLogQueryDTO queryDTO);
}
