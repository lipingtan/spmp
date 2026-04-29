package com.spmp.workorder.api;

import com.spmp.workorder.api.dto.WorkOrderBriefDTO;
import com.spmp.workorder.api.dto.WorkOrderOwnerDTO;
import com.spmp.workorder.api.dto.WorkOrderStatisticsDTO;

import java.util.List;

/**
 * 工单对外 API 接口（供其他模块调用）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface WorkOrderApi {

    /**
     * 工单摘要信息（供 notice、billing 调用）。
     *
     * @param workOrderId 工单ID
     * @return 工单摘要信息
     */
    WorkOrderBriefDTO getWorkOrderBrief(Long workOrderId);

    /**
     * 楼栋待处理工单数（供 base 统计展示调用）。
     *
     * @param buildingId 楼栋ID
     * @return 待处理工单数
     */
    int countPendingByBuildingId(Long buildingId);

    /**
     * 按小区统计工单数（供首页仪表板调用）。
     *
     * @param communityId 小区ID
     * @return 工单统计信息
     */
    WorkOrderStatisticsDTO countByCommunityId(Long communityId);

    /**
     * 业主的工单列表（供其他模块展示调用）。
     *
     * @param ownerId 业主ID
     * @return 工单列表
     */
    List<WorkOrderOwnerDTO> listByOwnerId(Long ownerId);
}
