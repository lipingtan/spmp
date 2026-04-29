package com.spmp.workorder.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spmp.common.result.PageResult;
import com.spmp.workorder.domain.dto.H5WorkOrderQueryDTO;
import com.spmp.workorder.domain.dto.WorkOrderCompleteDTO;
import com.spmp.workorder.domain.vo.RepairDashboardVO;
import com.spmp.workorder.domain.vo.WorkOrderSimpleVO;
import com.spmp.workorder.repository.WorkOrderMapper;
import com.spmp.workorder.service.H5RepairWorkOrderService;
import com.spmp.workorder.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * H5 维修人员端工单服务实现。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class H5RepairWorkOrderServiceImpl implements H5RepairWorkOrderService {

    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderService workOrderService;

    @Override
    public RepairDashboardVO getDashboard(Long repairUserId) {
        Map<String, Object> stats = workOrderMapper.countRepairStats(repairUserId);
        RepairDashboardVO vo = new RepairDashboardVO();
        if (stats != null) {
            Object pending = stats.get("pendingCount");
            Object monthly = stats.get("monthlyCompleted");
            vo.setTodayPendingCount(pending != null ? ((Number) pending).intValue() : 0);
            vo.setMonthlyCompletedCount(monthly != null ? ((Number) monthly).intValue() : 0);
        }
        return vo;
    }

    @Override
    public PageResult<WorkOrderSimpleVO> listPendingOrders(H5WorkOrderQueryDTO queryDTO, Long repairUserId) {
        IPage<WorkOrderSimpleVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<WorkOrderSimpleVO> result = workOrderMapper.selectRepairPendingPage(page, repairUserId, queryDTO.getStatus());
        return PageResult.of(result);
    }

    @Override
    public PageResult<WorkOrderSimpleVO> listHistoryOrders(H5WorkOrderQueryDTO queryDTO, Long repairUserId) {
        IPage<WorkOrderSimpleVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<WorkOrderSimpleVO> result = workOrderMapper.selectRepairHistoryPage(page, repairUserId);
        return PageResult.of(result);
    }

    @Override
    public void acceptWorkOrder(Long id, Long repairUserId) {
        workOrderService.acceptWorkOrder(id, repairUserId);
    }

    @Override
    public void completeWorkOrder(Long id, WorkOrderCompleteDTO completeDTO, Long repairUserId) {
        workOrderService.completeWorkOrder(id, completeDTO, repairUserId);
    }

    @Override
    public void transferWorkOrder(Long id, Long repairUserId, String reason) {
        workOrderService.transferWorkOrder(id, repairUserId, reason);
    }
}
