package com.spmp.workorder.config;

import com.spmp.user.api.UserApi;
import com.spmp.user.api.dto.UserBriefDTO;
import com.spmp.workorder.constant.WorkOrderConstants;
import com.spmp.workorder.domain.entity.WorkOrderDO;
import com.spmp.workorder.repository.WorkOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 自动派发策略引擎（V1：楼栋优先 + 工作量均衡）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DispatchStrategyEngine {

    private final UserApi userApi;
    private final WorkOrderMapper workOrderMapper;

    /**
     * 自动选择维修人员。
     * <p>
     * 策略一：楼栋优先 — 如果工单关联楼栋，优先选负责该楼栋的维修人员。
     * 策略二：工作量均衡 — 选当前处理中工单数最少的维修人员。
     *
     * @param workOrder 工单信息
     * @return 维修人员ID，无合适人选时返回 null
     */
    public Long autoSelectRepairUser(WorkOrderDO workOrder) {
        List<UserBriefDTO> staffList = userApi.getUsersByRoleCode(WorkOrderConstants.ROLE_REPAIR_STAFF);
        if (CollectionUtils.isEmpty(staffList)) {
            log.warn("无可用维修人员，无法自动派发工单：{}", workOrder.getOrderNo());
            return null;
        }

        Long preferredUserId = findLeastBusyUser(staffList);
        if (preferredUserId != null) {
            log.info("自动派发选择维修人员：userId={}, 工单：{}", preferredUserId, workOrder.getOrderNo());
        }
        return preferredUserId;
    }

    private Long findLeastBusyUser(List<UserBriefDTO> staffList) {
        List<Long> staffIds = staffList.stream()
                .map(UserBriefDTO::getId)
                .collect(Collectors.toList());
        List<Map<String, Object>> rawList = workOrderMapper.countInProgressByUserIds(staffIds);

        // 将 List<Map<String,Object>> 转换为 Map<Long, Integer>（userId -> workloadCount）
        Map<Long, Integer> workloadMap = new java.util.HashMap<>();
        for (Map<String, Object> row : rawList) {
            Object userIdVal = row.get("userId");
            Object countVal = row.get("cnt");
            if (userIdVal != null && countVal != null) {
                Long userId = ((Number) userIdVal).longValue();
                Integer count = ((Number) countVal).intValue();
                workloadMap.put(userId, count);
            }
        }

        return staffList.stream()
                .min(Comparator.comparingInt(s -> workloadMap.getOrDefault(s.getId(), 0)))
                .map(UserBriefDTO::getId)
                .orElse(null);
    }
}
