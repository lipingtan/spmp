package com.spmp.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spmp.user.domain.entity.OperationLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLogDO> {
}
