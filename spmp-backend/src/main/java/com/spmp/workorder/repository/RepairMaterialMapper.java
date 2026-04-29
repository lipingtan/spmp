package com.spmp.workorder.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spmp.workorder.domain.entity.RepairMaterialDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 维修材料 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface RepairMaterialMapper extends BaseMapper<RepairMaterialDO> {

    /**
     * 按工单ID查询维修材料列表。
     */
    List<RepairMaterialDO> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 按工单ID删除维修材料。
     */
    int deleteByOrderId(@Param("orderId") Long orderId);
}
