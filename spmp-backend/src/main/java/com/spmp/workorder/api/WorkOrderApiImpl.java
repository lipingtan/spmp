package com.spmp.workorder.api;

import com.spmp.workorder.api.dto.WorkOrderBriefDTO;
import com.spmp.workorder.api.dto.WorkOrderOwnerDTO;
import com.spmp.workorder.api.dto.WorkOrderStatisticsDTO;
import com.spmp.workorder.domain.entity.WorkOrderDO;
import com.spmp.workorder.repository.WorkOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 工单对外 API 实现。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkOrderApiImpl implements WorkOrderApi {

    private final WorkOrderMapper workOrderMapper;

    @Override
    public WorkOrderBriefDTO getWorkOrderBrief(Long workOrderId) {
        WorkOrderDO order = workOrderMapper.selectById(workOrderId);
        if (order == null) {
            return null;
        }
        WorkOrderBriefDTO dto = new WorkOrderBriefDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setOrderType(order.getOrderType());
        dto.setStatus(order.getStatus());
        dto.setDescription(order.getDescription());
        dto.setReporterName(order.getReporterName());
        dto.setCreateTime(toLocalDateTime(order.getCreateTime()));
        dto.setActualCompleteTime(order.getActualCompleteTime());
        return dto;
    }

    @Override
    public int countPendingByBuildingId(Long buildingId) {
        return workOrderMapper.countPendingByBuildingId(buildingId);
    }

    @Override
    public WorkOrderStatisticsDTO countByCommunityId(Long communityId) {
        int total = workOrderMapper.countByCommunityId(communityId);
        WorkOrderStatisticsDTO dto = new WorkOrderStatisticsDTO();
        dto.setTotalCount(total);
        return dto;
    }

    @Override
    public List<WorkOrderOwnerDTO> listByOwnerId(Long ownerId) {
        List<WorkOrderDO> orders = workOrderMapper.selectByOwnerId(ownerId);
        return orders.stream().map(order -> {
            WorkOrderOwnerDTO dto = new WorkOrderOwnerDTO();
            dto.setId(order.getId());
            dto.setOrderNo(order.getOrderNo());
            dto.setOrderType(order.getOrderType());
            dto.setStatus(order.getStatus());
            dto.setDescription(order.getDescription());
            dto.setCreateTime(toLocalDateTime(order.getCreateTime()));
            return dto;
        }).collect(Collectors.toList());
    }

    private LocalDateTime toLocalDateTime(java.util.Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
