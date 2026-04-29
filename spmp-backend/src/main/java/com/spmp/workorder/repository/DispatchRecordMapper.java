package com.spmp.workorder.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spmp.workorder.domain.entity.DispatchRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 派发记录 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface DispatchRecordMapper extends BaseMapper<DispatchRecordDO> {

    /**
     * 按工单ID查询派发记录（按时间倒序）。
     */
    List<DispatchRecordDO> selectByOrderId(@Param("orderId") Long orderId);
}
