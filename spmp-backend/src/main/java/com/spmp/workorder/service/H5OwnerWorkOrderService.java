package com.spmp.workorder.service;

import com.spmp.common.result.PageResult;
import com.spmp.workorder.domain.dto.H5WorkOrderQueryDTO;
import com.spmp.workorder.domain.dto.WorkOrderCancelDTO;
import com.spmp.workorder.domain.dto.WorkOrderCreateDTO;
import com.spmp.workorder.domain.dto.WorkOrderEvaluateDTO;
import com.spmp.workorder.domain.dto.WorkOrderVerifyDTO;
import com.spmp.workorder.domain.vo.WorkOrderDetailVO;
import com.spmp.workorder.domain.vo.WorkOrderSimpleVO;

/**
 * H5 业主端工单服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface H5OwnerWorkOrderService {

    Long createWorkOrder(WorkOrderCreateDTO createDTO, Long ownerId);

    PageResult<WorkOrderSimpleVO> listMyWorkOrders(H5WorkOrderQueryDTO queryDTO, Long ownerId);

    WorkOrderDetailVO getWorkOrderDetail(Long id, Long ownerId);

    void verifyWorkOrder(Long id, WorkOrderVerifyDTO verifyDTO, Long ownerId);

    void cancelWorkOrder(Long id, WorkOrderCancelDTO cancelDTO, Long ownerId);

    void urgeWorkOrder(Long id, Long ownerId);

    void evaluateWorkOrder(Long id, WorkOrderEvaluateDTO evaluateDTO, Long ownerId);
}
