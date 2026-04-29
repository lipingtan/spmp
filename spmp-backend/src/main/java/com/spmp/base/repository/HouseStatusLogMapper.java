package com.spmp.base.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spmp.base.domain.entity.HouseStatusLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 房屋状态变更历史 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface HouseStatusLogMapper extends BaseMapper<HouseStatusLogDO> {
}
