package com.spmp.workorder.service;

import com.spmp.common.result.PageResult;
import com.spmp.workorder.domain.dto.WorkOrderQueryDTO;
import com.spmp.workorder.domain.vo.WorkOrderDetailVO;
import com.spmp.workorder.domain.vo.WorkOrderListVO;

/**
 * 工单查询服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface WorkOrderQueryService {

    PageResult<WorkOrderListVO> listWorkOrders(WorkOrderQueryDTO queryDTO);

    WorkOrderDetailVO getWorkOrderDetail(Long id);
}
