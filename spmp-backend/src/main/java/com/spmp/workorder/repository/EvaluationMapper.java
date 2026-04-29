package com.spmp.workorder.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spmp.workorder.domain.entity.EvaluationDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 评价记录 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface EvaluationMapper extends BaseMapper<EvaluationDO> {

    /**
     * 按工单ID查询评价。
     */
    EvaluationDO selectByOrderId(@Param("orderId") Long orderId);
}
