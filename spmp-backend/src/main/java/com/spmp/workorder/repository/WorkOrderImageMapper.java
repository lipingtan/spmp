package com.spmp.workorder.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spmp.workorder.domain.entity.WorkOrderImageDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工单图片 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface WorkOrderImageMapper extends BaseMapper<WorkOrderImageDO> {

    /**
     * 按工单ID和图片类型查询图片列表。
     */
    List<WorkOrderImageDO> selectByOrderIdAndType(@Param("orderId") Long orderId, @Param("imageType") String imageType);

    /**
     * 按工单ID查询所有图片。
     */
    List<WorkOrderImageDO> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 按工单ID删除图片。
     */
    int deleteByOrderId(@Param("orderId") Long orderId);
}
