package com.spmp.workorder.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spmp.workorder.domain.entity.WorkOrderLogDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工单操作日志 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface WorkOrderLogMapper extends BaseMapper<WorkOrderLogDO> {

    /**
     * 按工单ID查询操作日志（按操作时间正序）。
     */
    List<WorkOrderLogDO> selectByOrderId(@Param("orderId") Long orderId);
}
