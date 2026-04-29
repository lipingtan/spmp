package com.spmp.workorder.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spmp.workorder.domain.entity.UrgeRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 催单记录 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface UrgeRecordMapper extends BaseMapper<UrgeRecordDO> {

    /**
     * 按工单ID查询催单记录（按时间倒序）。
     */
    List<UrgeRecordDO> selectByOrderId(@Param("orderId") Long orderId);
}
