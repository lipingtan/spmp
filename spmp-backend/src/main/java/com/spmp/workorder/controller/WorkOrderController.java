package com.spmp.workorder.controller;

import com.spmp.common.result.PageResult;
import com.spmp.common.result.Result;
import com.spmp.user.annotation.OperationLog;
import com.spmp.user.api.UserApi;
import com.spmp.user.api.dto.UserBriefDTO;
import com.spmp.workorder.domain.dto.WorkOrderCancelDTO;
import com.spmp.workorder.domain.dto.WorkOrderDispatchDTO;
import com.spmp.workorder.domain.dto.WorkOrderQueryDTO;
import com.spmp.workorder.domain.vo.RepairStaffVO;
import com.spmp.workorder.domain.vo.WorkOrderDetailVO;
import com.spmp.workorder.domain.vo.WorkOrderListVO;
import com.spmp.workorder.repository.WorkOrderMapper;
import com.spmp.workorder.service.WorkOrderQueryService;
import com.spmp.workorder.service.WorkOrderService;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工单管理 Controller（PC 管理端）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/workorder/orders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;
    private final WorkOrderQueryService workOrderQueryService;
    private final UserApi userApi;
    private final WorkOrderMapper workOrderMapper;

    /**
     * 工单列表（分页查询，数据权限过滤）。
     */
    @GetMapping
    @PreAuthorize("@perm.check('workorder:list')")
    public PageResult<WorkOrderListVO> listWorkOrders(WorkOrderQueryDTO queryDTO) {
        return workOrderQueryService.listWorkOrders(queryDTO);
    }

    /**
     * 工单详情。
     */
    @GetMapping("/{id}")
    @PreAuthorize("@perm.check('workorder:detail')")
    public Result<WorkOrderDetailVO> getWorkOrderDetail(@PathVariable Long id) {
        return Result.success(workOrderQueryService.getWorkOrderDetail(id));
    }

    /**
     * 派发工单。
     */
    @PutMapping("/{id}/dispatch")
    @PreAuthorize("@perm.check('workorder:dispatch')")
    @OperationLog(module = "工单管理", type = "DISPATCH", description = "派发工单")
    public Result<Void> dispatchWorkOrder(@PathVariable Long id,
                                          @Valid @RequestBody WorkOrderDispatchDTO dispatchDTO) {
        workOrderService.dispatchWorkOrder(id, dispatchDTO);
        return Result.success(null);
    }

    /**
     * 取消/关闭工单。
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("@perm.check('workorder:cancel')")
    @OperationLog(module = "工单管理", type = "CANCEL", description = "取消/关闭工单")
    public Result<Void> cancelWorkOrder(@PathVariable Long id,
                                        @Valid @RequestBody WorkOrderCancelDTO cancelDTO) {
        workOrderService.cancelWorkOrder(id, cancelDTO, null, "ADMIN");
        return Result.success(null);
    }

    /**
     * 获取维修人员列表（派发下拉选择）。
     */
    @GetMapping("/staff")
    @PreAuthorize("@perm.check('workorder:dispatch')")
    public Result<List<RepairStaffVO>> listRepairStaff(
            @RequestParam(required = false) Long communityId) {
        // 查询具有维修人员角色的用户
        List<UserBriefDTO> staffUsers = userApi.getUsersByRoleCode("repairman");
        if (staffUsers == null || staffUsers.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        // 查询每个维修人员当前处理中的工单数
        List<Long> userIds = staffUsers.stream()
                .map(UserBriefDTO::getId)
                .collect(Collectors.toList());

        // countInProgressByUserIds 返回 List<Map>，手动转换
        Map<Long, Integer> workloadMap = Collections.emptyMap();
        try {
            @SuppressWarnings("unchecked")
            Object rawResult = workOrderMapper.countInProgressByUserIds(userIds);
            if (rawResult instanceof Map) {
                workloadMap = (Map<Long, Integer>) rawResult;
            }
        } catch (Exception e) {
            // 降级：工单数统计失败不影响人员列表展示
        }

        // 组装 VO
        List<RepairStaffVO> result = new ArrayList<>();
        for (UserBriefDTO user : staffUsers) {
            RepairStaffVO vo = new RepairStaffVO();
            vo.setUserId(user.getId());
            vo.setRealName(user.getRealName());
            vo.setPhone(user.getPhone());
            Integer workload = workloadMap.get(user.getId());
            vo.setCurrentWorkload(workload != null ? workload : 0);
            result.add(vo);
        }
        return Result.success(result);
    }
}
