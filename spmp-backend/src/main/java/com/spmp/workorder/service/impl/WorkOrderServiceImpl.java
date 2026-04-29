package com.spmp.workorder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.spmp.common.exception.BusinessException;
import com.spmp.workorder.config.WorkOrderEventPublisher;
import com.spmp.workorder.config.WorkOrderStateMachine;
import com.spmp.workorder.constant.*;
import com.spmp.workorder.domain.dto.RepairMaterialDTO;
import com.spmp.workorder.domain.dto.WorkOrderCancelDTO;
import com.spmp.workorder.domain.dto.WorkOrderCompleteDTO;
import com.spmp.workorder.domain.dto.WorkOrderDispatchDTO;
import com.spmp.workorder.domain.dto.WorkOrderVerifyDTO;
import com.spmp.workorder.domain.entity.*;
import com.spmp.workorder.repository.*;
import com.spmp.workorder.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 工单核心服务实现。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderImageMapper workOrderImageMapper;
    private final DispatchRecordMapper dispatchRecordMapper;
    private final RepairMaterialMapper repairMaterialMapper;
    private final EvaluationMapper evaluationMapper;
    private final WorkOrderLogMapper workOrderLogMapper;
    private final WorkOrderStateMachine stateMachine;
    private final WorkOrderEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dispatchWorkOrder(Long id, WorkOrderDispatchDTO dispatchDTO) {
        WorkOrderDO order = getWorkOrderOrThrow(id);
        stateMachine.checkTransition(
                WorkOrderStatus.fromCode(order.getStatus()),
                WorkOrderStatus.PENDING_ACCEPT
        );

        order.setRepairUserId(dispatchDTO.getRepairUserId());
        order.setStatus(WorkOrderStatus.PENDING_ACCEPT.getCode());
        if (dispatchDTO.getExpectedCompleteTime() != null) {
            order.setExpectedCompleteTime(dispatchDTO.getExpectedCompleteTime());
        }
        workOrderMapper.updateById(order);

        DispatchRecordDO record = new DispatchRecordDO();
        record.setOrderId(id);
        record.setRepairUserId(dispatchDTO.getRepairUserId());
        record.setDispatchType(DispatchType.MANUAL.getCode());
        record.setRemark(dispatchDTO.getRemark());
        record.setDispatchTime(LocalDateTime.now());
        dispatchRecordMapper.insert(record);

        saveLog(id, WorkOrderAction.DISPATCH, WorkOrderStatus.PENDING_DISPATCH.getCode(),
                WorkOrderStatus.PENDING_ACCEPT.getCode(), null, "手动派发");

        eventPublisher.publishDispatch(id, dispatchDTO.getRepairUserId(), order.getOrderNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelWorkOrder(Long id, WorkOrderCancelDTO cancelDTO, Long operatorId, String operatorType) {
        WorkOrderDO order = getWorkOrderOrThrow(id);

        WorkOrderStatus current = WorkOrderStatus.fromCode(order.getStatus());
        String cancelType = cancelDTO.getCancelType();
        WorkOrderStatus targetStatus;
        if ("FORCE_CLOSE".equals(cancelType)) {
            targetStatus = WorkOrderStatus.FORCE_CLOSED;
        } else {
            targetStatus = WorkOrderStatus.CANCELLED;
        }
        stateMachine.checkTransition(current, targetStatus);

        String fromStatus = order.getStatus();
        order.setStatus(targetStatus.getCode());
        order.setCancelReason(cancelDTO.getCancelReason());
        order.setCancelBy(operatorId);
        workOrderMapper.updateById(order);

        saveLog(id, "FORCE_CLOSE".equals(cancelType) ? WorkOrderAction.FORCE_CLOSE : WorkOrderAction.CANCEL,
                fromStatus, targetStatus.getCode(), operatorId, cancelDTO.getCancelReason());

        if (order.getRepairUserId() != null) {
            eventPublisher.publishCancel(id, order.getRepairUserId(), order.getOrderNo());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptWorkOrder(Long id, Long repairUserId) {
        WorkOrderDO order = getWorkOrderOrThrow(id);
        if (!WorkOrderStatus.PENDING_ACCEPT.getCode().equals(order.getStatus())) {
            throw new BusinessException(WorkOrderErrorCode.INVALID_STATUS_TRANSITION.getCode(), "工单状态不是待接单");
        }
        if (!repairUserId.equals(order.getRepairUserId())) {
            throw new BusinessException(WorkOrderErrorCode.WORK_ORDER_NOT_YOURS.getCode(), "该工单未派发给当前维修人员");
        }

        String fromStatus = order.getStatus();
        order.setStatus(WorkOrderStatus.IN_PROGRESS.getCode());
        order.setActualStartTime(LocalDateTime.now());
        workOrderMapper.updateById(order);

        saveLog(id, WorkOrderAction.ACCEPT, fromStatus,
                WorkOrderStatus.IN_PROGRESS.getCode(), repairUserId, "接单");

        eventPublisher.publishAccept(id, order.getReporterId(), order.getOrderNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeWorkOrder(Long id, WorkOrderCompleteDTO completeDTO, Long repairUserId) {
        WorkOrderDO order = getWorkOrderOrThrow(id);
        if (!WorkOrderStatus.IN_PROGRESS.getCode().equals(order.getStatus())) {
            throw new BusinessException(WorkOrderErrorCode.INVALID_STATUS_TRANSITION.getCode(), "工单状态不是处理中");
        }
        if (!repairUserId.equals(order.getRepairUserId())) {
            throw new BusinessException(WorkOrderErrorCode.WORK_ORDER_NOT_YOURS.getCode(), "该工单未派发给当前维修人员");
        }

        String fromStatus = order.getStatus();
        order.setStatus(WorkOrderStatus.PENDING_VERIFY.getCode());
        order.setActualCompleteTime(LocalDateTime.now());
        order.setRepairDuration(completeDTO.getRepairDuration());
        workOrderMapper.updateById(order);

        if (!CollectionUtils.isEmpty(completeDTO.getImageUrls())) {
            int sortOrder = 0;
            for (String url : completeDTO.getImageUrls()) {
                WorkOrderImageDO image = new WorkOrderImageDO();
                image.setOrderId(id);
                image.setImageUrl(url);
                image.setImageType(ImageType.REPAIR.getCode());
                image.setSortOrder(sortOrder++);
                workOrderImageMapper.insert(image);
            }
        }

        if (!CollectionUtils.isEmpty(completeDTO.getMaterials())) {
            for (RepairMaterialDTO mat : completeDTO.getMaterials()) {
                RepairMaterialDO material = new RepairMaterialDO();
                material.setOrderId(id);
                material.setMaterialName(mat.getMaterialName());
                material.setQuantity(mat.getQuantity());
                material.setUnit(mat.getUnit());
                material.setUnitPrice(mat.getUnitPrice());
                material.setTotalPrice(mat.getQuantity().multiply(mat.getUnitPrice()));
                repairMaterialMapper.insert(material);
            }
        }

        saveLog(id, WorkOrderAction.COMPLETE, fromStatus,
                WorkOrderStatus.PENDING_VERIFY.getCode(), repairUserId, "维修完成");

        eventPublisher.publishComplete(id, order.getReporterId(), order.getOrderNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferWorkOrder(Long id, Long repairUserId, String reason) {
        WorkOrderDO order = getWorkOrderOrThrow(id);
        if (!WorkOrderStatus.IN_PROGRESS.getCode().equals(order.getStatus())) {
            throw new BusinessException(WorkOrderErrorCode.INVALID_STATUS_TRANSITION.getCode(), "只有处理中的工单可以转派");
        }
        if (!repairUserId.equals(order.getRepairUserId())) {
            throw new BusinessException(WorkOrderErrorCode.WORK_ORDER_NOT_YOURS.getCode(), "非本人工单");
        }

        String fromStatus = order.getStatus();
        order.setStatus(WorkOrderStatus.PENDING_DISPATCH.getCode());
        order.setRepairUserId(null);
        workOrderMapper.updateById(order);

        saveLog(id, WorkOrderAction.TRANSFER, fromStatus,
                WorkOrderStatus.PENDING_DISPATCH.getCode(), repairUserId, "转派原因：" + reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyWorkOrder(Long id, WorkOrderVerifyDTO verifyDTO, Long ownerId) {
        WorkOrderDO order = getWorkOrderOrThrow(id);
        if (!WorkOrderStatus.PENDING_VERIFY.getCode().equals(order.getStatus())) {
            throw new BusinessException(WorkOrderErrorCode.INVALID_STATUS_TRANSITION.getCode(), "工单状态不是待验收");
        }
        if (!ownerId.equals(order.getReporterId())) {
            throw new BusinessException(WorkOrderErrorCode.WORK_ORDER_NOT_YOURS.getCode(), "非本人工单");
        }

        String fromStatus = order.getStatus();

        if (Boolean.TRUE.equals(verifyDTO.getPassed())) {
            order.setStatus(WorkOrderStatus.COMPLETED.getCode());
            workOrderMapper.updateById(order);

            if (verifyDTO.getScore() != null) {
                EvaluationDO evaluation = new EvaluationDO();
                evaluation.setOrderId(id);
                evaluation.setScore(verifyDTO.getScore());
                evaluation.setContent(verifyDTO.getEvaluateContent());
                evaluation.setEvaluatorId(ownerId);
                evaluation.setEvaluateType(EvaluateType.OWNER.getCode());
                evaluationMapper.insert(evaluation);
            }

            saveLog(id, WorkOrderAction.VERIFY_PASS, fromStatus,
                    WorkOrderStatus.COMPLETED.getCode(), ownerId, "验收通过");
        } else {
            if (!StringUtils.hasText(verifyDTO.getRejectReason())) {
                throw new BusinessException(WorkOrderErrorCode.VERIFY_REJECT_REASON_REQUIRED.getCode(),
                        WorkOrderErrorCode.VERIFY_REJECT_REASON_REQUIRED.getMessage());
            }
            if (CollectionUtils.isEmpty(verifyDTO.getRejectImageUrls())) {
                throw new BusinessException(WorkOrderErrorCode.VERIFY_REJECT_IMAGE_REQUIRED.getCode(),
                        WorkOrderErrorCode.VERIFY_REJECT_IMAGE_REQUIRED.getMessage());
            }

            int currentRejectCount = order.getRejectCount() != null ? order.getRejectCount() : 0;
            int newRejectCount = currentRejectCount + 1;
            if (newRejectCount > WorkOrderConstants.MAX_REJECT_COUNT) {
                throw new BusinessException(WorkOrderErrorCode.REJECT_LIMIT_EXCEEDED.getCode(),
                        "验收不通过次数已达上限（" + WorkOrderConstants.MAX_REJECT_COUNT + "次）");
            }

            order.setStatus(WorkOrderStatus.IN_PROGRESS.getCode());
            order.setRejectCount(newRejectCount);
            workOrderMapper.updateById(order);

            if (!CollectionUtils.isEmpty(verifyDTO.getRejectImageUrls())) {
                int sortOrder = 0;
                for (String url : verifyDTO.getRejectImageUrls()) {
                    WorkOrderImageDO image = new WorkOrderImageDO();
                    image.setOrderId(id);
                    image.setImageUrl(url);
                    image.setImageType(ImageType.REJECT.getCode());
                    image.setSortOrder(sortOrder++);
                    workOrderImageMapper.insert(image);
                }
            }

            saveLog(id, WorkOrderAction.VERIFY_REJECT, fromStatus,
                    WorkOrderStatus.IN_PROGRESS.getCode(), ownerId,
                    "验收不通过（第" + newRejectCount + "次）：" + verifyDTO.getRejectReason());

            eventPublisher.publishVerifyReject(id, order.getRepairUserId(), order.getOrderNo(), newRejectCount);

            if (newRejectCount >= WorkOrderConstants.MAX_REJECT_COUNT) {
                eventPublisher.publishRejectEscalate(id, order.getCommunityId(), order.getOrderNo());
            }
        }
    }

    private WorkOrderDO getWorkOrderOrThrow(Long id) {
        WorkOrderDO order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(WorkOrderErrorCode.WORK_ORDER_NOT_FOUND.getCode(),
                    WorkOrderErrorCode.WORK_ORDER_NOT_FOUND.getMessage());
        }
        return order;
    }

    private void saveLog(Long orderId, WorkOrderAction action, String fromStatus,
                          String toStatus, Long operatorId, String remark) {
        WorkOrderLogDO logDO = new WorkOrderLogDO();
        logDO.setOrderId(orderId);
        logDO.setAction(action.getCode());
        logDO.setFromStatus(fromStatus);
        logDO.setToStatus(toStatus);
        logDO.setOperatorId(operatorId);
        logDO.setOperatorType("SYSTEM");
        logDO.setRemark(remark);
        logDO.setOperateTime(LocalDateTime.now());
        workOrderLogMapper.insert(logDO);
    }
}
