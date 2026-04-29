package com.spmp.workorder.config;

import com.spmp.workorder.constant.EvaluateType;
import com.spmp.workorder.constant.WorkOrderAction;
import com.spmp.workorder.constant.WorkOrderConstants;
import com.spmp.workorder.constant.WorkOrderStatus;
import com.spmp.workorder.domain.entity.EvaluationDO;
import com.spmp.workorder.domain.entity.WorkOrderDO;
import com.spmp.workorder.domain.entity.WorkOrderLogDO;
import com.spmp.workorder.repository.EvaluationMapper;
import com.spmp.workorder.repository.WorkOrderLogMapper;
import com.spmp.workorder.repository.WorkOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * 自动验收定时任务。
 * <p>
 * 每小时检测一次：
 * - 待验收状态超过 7 天的工单自动验收完成，默认 5 星评价
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AutoVerifyTask {

    private final WorkOrderMapper workOrderMapper;
    private final EvaluationMapper evaluationMapper;
    private final WorkOrderLogMapper workOrderLogMapper;

    @Scheduled(fixedRate = 60 * 60 * 1000)
    @Transactional(rollbackFor = Exception.class)
    public void autoVerify() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(WorkOrderConstants.AUTO_VERIFY_DAYS);
        Date deadlineDate = Date.from(deadline.atZone(ZoneId.systemDefault()).toInstant());

        List<WorkOrderDO> orders = workOrderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WorkOrderDO>()
                        .eq(WorkOrderDO::getStatus, WorkOrderStatus.PENDING_VERIFY.getCode())
                        .le(WorkOrderDO::getActualCompleteTime, deadlineDate)
        );

        for (WorkOrderDO order : orders) {
            String fromStatus = order.getStatus();
            order.setStatus(WorkOrderStatus.COMPLETED.getCode());
            workOrderMapper.updateById(order);

            EvaluationDO evaluation = new EvaluationDO();
            evaluation.setOrderId(order.getId());
            evaluation.setScore(5);
            evaluation.setEvaluateType(EvaluateType.AUTO.getCode());
            evaluation.setEvaluatorId(order.getReporterId());
            evaluationMapper.insert(evaluation);

            WorkOrderLogDO logDO = new WorkOrderLogDO();
            logDO.setOrderId(order.getId());
            logDO.setAction(WorkOrderAction.AUTO_VERIFY.getCode());
            logDO.setFromStatus(fromStatus);
            logDO.setToStatus(WorkOrderStatus.COMPLETED.getCode());
            logDO.setOperatorType("SYSTEM");
            logDO.setRemark("超过" + WorkOrderConstants.AUTO_VERIFY_DAYS + "天未验收，自动完成");
            logDO.setOperateTime(LocalDateTime.now());
            workOrderLogMapper.insert(logDO);
        }

        if (!orders.isEmpty()) {
            log.info("自动验收完成，处理 {} 条工单", orders.size());
        }
    }
}
