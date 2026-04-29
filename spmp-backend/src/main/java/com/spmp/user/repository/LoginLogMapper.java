package com.spmp.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spmp.user.domain.entity.LoginLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLogDO> {
}
