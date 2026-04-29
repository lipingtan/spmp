package com.spmp.workorder.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.spmp.common.security.DataPermission;
import com.spmp.workorder.domain.dto.WorkOrderQueryDTO;
import com.spmp.workorder.domain.entity.WorkOrderDO;
import com.spmp.workorder.domain.vo.TrendDataVO;
import com.spmp.workorder.domain.vo.WorkOrderListVO;
import com.spmp.workorder.domain.vo.WorkOrderSimpleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 工单 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface WorkOrderMapper extends BaseMapper<WorkOrderDO> {

    /**
     * 分页查询工单列表（PC端，数据权限过滤）。
     */
    @DataPermission(communityField = "wo.community_id")
    IPage<WorkOrderListVO> selectWorkOrderPage(IPage<?> page, @Param("query") WorkOrderQueryDTO queryDTO);

    /**
     * 统计指定维修人员列表的当前处理中工单数。
     */
    List<Map<String, Object>> countInProgressByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * 按状态统计工单数量。
     */
    @DataPermission
    List<Map<String, Object>> countByStatus(@Param("communityId") Long communityId,
                                             @Param("buildingId") Long buildingId,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    /**
     * 计算平均维修时长（分钟）。
     */
    @DataPermission
    Integer avgRepairDuration(@Param("communityId") Long communityId,
                               @Param("buildingId") Long buildingId,
                               @Param("startDate") LocalDateTime startDate,
                               @Param("endDate") LocalDateTime endDate);

    /**
     * 计算平均满意度评分。
     */
    @DataPermission(communityField = "wo.community_id", buildingField = "wo.building_id")
    Double avgScore(@Param("communityId") Long communityId,
                     @Param("buildingId") Long buildingId,
                     @Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate);

    /**
     * 按日期统计工单量趋势。
     */
    @DataPermission
    List<TrendDataVO> countDailyTrend(@Param("communityId") Long communityId,
                                       @Param("buildingId") Long buildingId,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * 按月统计满意度趋势。
     */
    @DataPermission(communityField = "wo.community_id", buildingField = "wo.building_id")
    List<TrendDataVO> avgScoreMonthlyTrend(@Param("communityId") Long communityId,
                                            @Param("buildingId") Long buildingId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    /**
     * 统计维修人员待处理/本月完成工单数。
     */
    Map<String, Object> countRepairStats(@Param("repairUserId") Long repairUserId);

    /**
     * 查询超时未接单的工单列表。
     */
    List<WorkOrderDO> selectTimeoutPendingOrders(@Param("remindTime") LocalDateTime remindTime,
                                                   @Param("returnTime") LocalDateTime returnTime);

    /**
     * H5 业主端 — 我的工单分页查询。
     */
    IPage<WorkOrderSimpleVO> selectMyWorkOrderPage(IPage<?> page, @Param("ownerId") Long ownerId,
                                                     @Param("status") String status);

    /**
     * H5 维修人员端 — 待处理工单分页查询。
     */
    IPage<WorkOrderSimpleVO> selectRepairPendingPage(IPage<?> page, @Param("repairUserId") Long repairUserId,
                                                       @Param("status") String status);

    /**
     * H5 维修人员端 — 历史工单分页查询。
     */
    IPage<WorkOrderSimpleVO> selectRepairHistoryPage(IPage<?> page, @Param("repairUserId") Long repairUserId);

    /**
     * 按小区统计工单数。
     */
    int countByCommunityId(@Param("communityId") Long communityId);

    /**
     * 按楼栋统计待处理工单数。
     */
    int countPendingByBuildingId(@Param("buildingId") Long buildingId);

    /**
     * 按业主查询工单列表（对外API）。
     */
    List<WorkOrderDO> selectByOwnerId(@Param("ownerId") Long ownerId);
}
