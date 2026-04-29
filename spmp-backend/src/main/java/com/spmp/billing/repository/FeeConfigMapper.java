package com.spmp.billing.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spmp.billing.domain.entity.FeeConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 费用配置 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface FeeConfigMapper extends BaseMapper<FeeConfigDO> {
}
