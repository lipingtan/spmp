package com.spmp.workorder.controller.h5.repair;

import com.spmp.common.result.PageResult;
import com.spmp.common.result.Result;
import com.spmp.common.util.SecurityUtils;
import com.spmp.user.annotation.OperationLog;
import com.spmp.workorder.domain.dto.H5WorkOrderQueryDTO;
import com.spmp.workorder.domain.dto.WorkOrderCompleteDTO;
import com.spmp.workorder.domain.vo.RepairDashboardVO;
import com.spmp.workorder.domain.vo.WorkOrderDetailVO;
import com.spmp.workorder.domain.vo.WorkOrderSimpleVO;
import com.spmp.workorder.service.H5RepairWorkOrderService;
import com.spmp.workorder.service.WorkOrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * H5 维修人员端 — 工单处理 Controller。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/workorder/h5/repair")
@RequiredArgsConstructor
public class H5RepairController {

    private final H5RepairWorkOrderService h5RepairWorkOrderService;
    private final WorkOrderQueryService workOrderQueryService;

    /**
     * 工作台。
     */
    @GetMapping("/dashboard")
    public Result<RepairDashboardVO> getDashboard() {
        Long repairUserId = SecurityUtils.getCurrentUserId();
        return Result.success(h5RepairWorkOrderService.getDashboard(repairUserId));
    }

    /**
     * 工单详情（维修人员端）。
     */
    @GetMapping("/orders/{id}")
    public Result<WorkOrderDetailVO> getWorkOrderDetail(@PathVariable Long id) {
        return Result.success(workOrderQueryService.getWorkOrderDetail(id));
    }

    /**
     * 待处理工单列表。
     */
    @GetMapping("/pending")
    public PageResult<WorkOrderSimpleVO> listPendingOrders(H5WorkOrderQueryDTO queryDTO) {
        Long repairUserId = SecurityUtils.getCurrentUserId();
        return h5RepairWorkOrderService.listPendingOrders(queryDTO, repairUserId);
    }

    /**
     * 历史工单列表。
     */
    @GetMapping("/history")
    public PageResult<WorkOrderSimpleVO> listHistoryOrders(H5WorkOrderQueryDTO queryDTO) {
        Long repairUserId = SecurityUtils.getCurrentUserId();
        return h5RepairWorkOrderService.listHistoryOrders(queryDTO, repairUserId);
    }

    /**
     * 接单。
     */
    @PutMapping("/orders/{id}/accept")
    @OperationLog(module = "工单管理", type = "ACCEPT", description = "接单")
    public Result<Void> acceptWorkOrder(@PathVariable Long id) {
        Long repairUserId = SecurityUtils.getCurrentUserId();
        h5RepairWorkOrderService.acceptWorkOrder(id, repairUserId);
        return Result.success(null);
    }

    /**
     * 完成维修。
     */
    @PutMapping("/orders/{id}/complete")
    @OperationLog(module = "工单管理", type = "COMPLETE", description = "完成维修")
    public Result<Void> completeWorkOrder(@PathVariable Long id,
                                          @Valid @RequestBody WorkOrderCompleteDTO completeDTO) {
        Long repairUserId = SecurityUtils.getCurrentUserId();
        h5RepairWorkOrderService.completeWorkOrder(id, completeDTO, repairUserId);
        return Result.success(null);
    }

    /**
     * 转派工单。
     */
    @PutMapping("/orders/{id}/transfer")
    @OperationLog(module = "工单管理", type = "TRANSFER", description = "转派工单")
    public Result<Void> transferWorkOrder(@PathVariable Long id,
                                          @RequestParam String reason) {
        Long repairUserId = SecurityUtils.getCurrentUserId();
        h5RepairWorkOrderService.transferWorkOrder(id, repairUserId, reason);
        return Result.success(null);
    }
}
