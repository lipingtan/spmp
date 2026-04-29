package com.spmp.workorder.service;

import com.spmp.common.result.PageResult;
import com.spmp.workorder.domain.dto.H5WorkOrderQueryDTO;
import com.spmp.workorder.domain.dto.WorkOrderCompleteDTO;
import com.spmp.workorder.domain.vo.RepairDashboardVO;
import com.spmp.workorder.domain.vo.WorkOrderSimpleVO;

/**
 * H5 维修人员端工单服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface H5RepairWorkOrderService {

    RepairDashboardVO getDashboard(Long repairUserId);

    PageResult<WorkOrderSimpleVO> listPendingOrders(H5WorkOrderQueryDTO queryDTO, Long repairUserId);

    PageResult<WorkOrderSimpleVO> listHistoryOrders(H5WorkOrderQueryDTO queryDTO, Long repairUserId);

    void acceptWorkOrder(Long id, Long repairUserId);

    void completeWorkOrder(Long id, WorkOrderCompleteDTO completeDTO, Long repairUserId);

    void transferWorkOrder(Long id, Long repairUserId, String reason);
}
