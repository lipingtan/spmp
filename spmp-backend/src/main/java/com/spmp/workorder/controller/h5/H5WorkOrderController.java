package com.spmp.workorder.controller.h5;

import com.spmp.common.result.PageResult;
import com.spmp.common.result.Result;
import com.spmp.common.util.SecurityUtils;
import com.spmp.user.annotation.OperationLog;
import com.spmp.workorder.domain.dto.H5WorkOrderQueryDTO;
import com.spmp.workorder.domain.dto.WorkOrderCancelDTO;
import com.spmp.workorder.domain.dto.WorkOrderCreateDTO;
import com.spmp.workorder.domain.dto.WorkOrderEvaluateDTO;
import com.spmp.workorder.domain.dto.WorkOrderVerifyDTO;
import com.spmp.workorder.domain.vo.WorkOrderDetailVO;
import com.spmp.workorder.domain.vo.WorkOrderSimpleVO;
import com.spmp.workorder.service.H5OwnerWorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * H5 业主端 — 工单管理 Controller。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/workorder/h5/orders")
@RequiredArgsConstructor
public class H5WorkOrderController {

    private final H5OwnerWorkOrderService h5OwnerWorkOrderService;

    /**
     * 提交报修。
     */
    @PostMapping
    @OperationLog(module = "工单管理", type = "CREATE", description = "提交报修")
    public Result<Long> createWorkOrder(@Valid @RequestBody WorkOrderCreateDTO createDTO) {
        Long ownerId = SecurityUtils.getCurrentOwnerId();
        return Result.success(h5OwnerWorkOrderService.createWorkOrder(createDTO, ownerId));
    }

    /**
     * 我的工单列表。
     */
    @GetMapping("/mine")
    public PageResult<WorkOrderSimpleVO> listMyWorkOrders(H5WorkOrderQueryDTO queryDTO) {
        Long ownerId = SecurityUtils.getCurrentOwnerId();
        return h5OwnerWorkOrderService.listMyWorkOrders(queryDTO, ownerId);
    }

    /**
     * 工单详情。
     */
    @GetMapping("/{id}")
    public Result<WorkOrderDetailVO> getWorkOrderDetail(@PathVariable Long id) {
        Long ownerId = SecurityUtils.getCurrentOwnerId();
        return Result.success(h5OwnerWorkOrderService.getWorkOrderDetail(id, ownerId));
    }

    /**
     * 验收工单。
     */
    @PutMapping("/{id}/verify")
    @OperationLog(module = "工单管理", type = "VERIFY", description = "验收工单")
    public Result<Void> verifyWorkOrder(@PathVariable Long id,
                                        @Valid @RequestBody WorkOrderVerifyDTO verifyDTO) {
        Long ownerId = SecurityUtils.getCurrentOwnerId();
        h5OwnerWorkOrderService.verifyWorkOrder(id, verifyDTO, ownerId);
        return Result.success(null);
    }

    /**
     * 评价工单。
     */
    @PostMapping("/{id}/evaluate")
    @OperationLog(module = "工单管理", type = "EVALUATE", description = "评价工单")
    public Result<Void> evaluateWorkOrder(@PathVariable Long id,
                                          @Valid @RequestBody WorkOrderEvaluateDTO evaluateDTO) {
        Long ownerId = SecurityUtils.getCurrentOwnerId();
        h5OwnerWorkOrderService.evaluateWorkOrder(id, evaluateDTO, ownerId);
        return Result.success(null);
    }

    /**
     * 催单。
     */
    @PostMapping("/{id}/urge")
    @OperationLog(module = "工单管理", type = "URGE", description = "催单")
    public Result<Void> urgeWorkOrder(@PathVariable Long id) {
        Long ownerId = SecurityUtils.getCurrentOwnerId();
        h5OwnerWorkOrderService.urgeWorkOrder(id, ownerId);
        return Result.success(null);
    }

    /**
     * 取消工单。
     */
    @PutMapping("/{id}/cancel")
    @OperationLog(module = "工单管理", type = "CANCEL", description = "取消工单")
    public Result<Void> cancelWorkOrder(@PathVariable Long id,
                                        @Valid @RequestBody WorkOrderCancelDTO cancelDTO) {
        Long ownerId = SecurityUtils.getCurrentOwnerId();
        h5OwnerWorkOrderService.cancelWorkOrder(id, cancelDTO, ownerId);
        return Result.success(null);
    }
}
