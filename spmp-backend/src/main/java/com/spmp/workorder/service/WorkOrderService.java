package com.spmp.workorder.service;

import com.spmp.workorder.domain.dto.WorkOrderCancelDTO;
import com.spmp.workorder.domain.dto.WorkOrderCompleteDTO;
import com.spmp.workorder.domain.dto.WorkOrderDispatchDTO;
import com.spmp.workorder.domain.dto.WorkOrderVerifyDTO;

/**
 * 工单核心服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface WorkOrderService {

    void dispatchWorkOrder(Long id, WorkOrderDispatchDTO dispatchDTO);

    void cancelWorkOrder(Long id, WorkOrderCancelDTO cancelDTO, Long operatorId, String operatorType);

    void acceptWorkOrder(Long id, Long repairUserId);

    void completeWorkOrder(Long id, WorkOrderCompleteDTO completeDTO, Long repairUserId);

    void transferWorkOrder(Long id, Long repairUserId, String reason);

    void verifyWorkOrder(Long id, WorkOrderVerifyDTO verifyDTO, Long ownerId);
}
