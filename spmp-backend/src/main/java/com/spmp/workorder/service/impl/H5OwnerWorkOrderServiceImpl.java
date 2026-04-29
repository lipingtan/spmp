package com.spmp.workorder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spmp.common.exception.BusinessException;
import com.spmp.common.result.PageResult;
import com.spmp.owner.api.OwnerApi;
import com.spmp.owner.api.dto.OwnerBriefDTO;
import com.spmp.workorder.config.OrderNoGenerator;
import com.spmp.workorder.config.WorkOrderEventPublisher;
import com.spmp.workorder.config.WorkOrderStateMachine;
import com.spmp.workorder.constant.*;
import com.spmp.workorder.domain.dto.H5WorkOrderQueryDTO;
import com.spmp.workorder.domain.dto.WorkOrderCancelDTO;
import com.spmp.workorder.domain.dto.WorkOrderCreateDTO;
import com.spmp.workorder.domain.dto.WorkOrderEvaluateDTO;
import com.spmp.workorder.domain.dto.WorkOrderVerifyDTO;
import com.spmp.workorder.domain.entity.EvaluationDO;
import com.spmp.workorder.domain.entity.WorkOrderDO;
import com.spmp.workorder.domain.entity.WorkOrderImageDO;
import com.spmp.workorder.domain.entity.WorkOrderLogDO;
import com.spmp.workorder.domain.vo.WorkOrderDetailVO;
import com.spmp.workorder.domain.vo.WorkOrderSimpleVO;
import com.spmp.workorder.repository.WorkOrderImageMapper;
import com.spmp.workorder.repository.EvaluationMapper;
import com.spmp.workorder.repository.WorkOrderLogMapper;
import com.spmp.workorder.repository.WorkOrderMapper;
import com.spmp.workorder.service.H5OwnerWorkOrderService;
import com.spmp.workorder.service.WorkOrderQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * H5 业主端工单服务实现。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class H5OwnerWorkOrderServiceImpl implements H5OwnerWorkOrderService {

    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderImageMapper workOrderImageMapper;
    private final WorkOrderLogMapper workOrderLogMapper;
    private final OrderNoGenerator orderNoGenerator;
    private final WorkOrderStateMachine stateMachine;
    private final WorkOrderEventPublisher eventPublisher;
    private final OwnerApi ownerApi;
    private final WorkOrderQueryService queryService;
    private final EvaluationMapper evaluationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWorkOrder(WorkOrderCreateDTO createDTO, Long ownerId) {
        OwnerBriefDTO owner = ownerApi.getOwnerById(ownerId);
        if (owner == null) {
            throw new BusinessException(WorkOrderErrorCode.WORK_ORDER_NOT_FOUND.getCode(),
                    WorkOrderErrorCode.WORK_ORDER_NOT_FOUND.getMessage());
        }

        String orderNo = orderNoGenerator.generate();

        WorkOrderDO order = new WorkOrderDO();
        order.setOrderNo(orderNo);
        order.setOrderType(createDTO.getOrderType());
        order.setAddressType(createDTO.getAddressType());
        order.setCommunityId(createDTO.getCommunityId());
        order.setHouseId(createDTO.getHouseId());
        order.setBuildingId(createDTO.getBuildingId());
        order.setUnitId(createDTO.getUnitId());
        order.setReporterId(ownerId);
        order.setReporterName(owner.getOwnerName());
        order.setReporterPhone(owner.getPhoneMasked());
        order.setDescription(createDTO.getDescription());
        order.setStatus(WorkOrderStatus.PENDING_DISPATCH.getCode());
        order.setRejectCount(0);
        order.setUrgeCount(0);
        workOrderMapper.insert(order);

        if (!CollectionUtils.isEmpty(createDTO.getImageUrls())) {
            if (createDTO.getImageUrls().size() > WorkOrderConstants.MAX_IMAGES_PER_TYPE) {
                throw new BusinessException(WorkOrderErrorCode.IMAGE_LIMIT_EXCEEDED.getCode(),
                        WorkOrderErrorCode.IMAGE_LIMIT_EXCEEDED.getMessage());
            }
            int sortOrder = 0;
            for (String url : createDTO.getImageUrls()) {
                WorkOrderImageDO image = new WorkOrderImageDO();
                image.setOrderId(order.getId());
                image.setImageUrl(url);
                image.setImageType(ImageType.REPORT.getCode());
                image.setSortOrder(sortOrder++);
                workOrderImageMapper.insert(image);
            }
        }

        saveLog(order.getId(), WorkOrderAction.CREATE, null,
                WorkOrderStatus.PENDING_DISPATCH.getCode(), ownerId, "业主提交报修");

        return order.getId();
    }

    @Override
    public PageResult<WorkOrderSimpleVO> listMyWorkOrders(H5WorkOrderQueryDTO queryDTO, Long ownerId) {
        IPage<WorkOrderSimpleVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<WorkOrderSimpleVO> result = workOrderMapper.selectMyWorkOrderPage(page, ownerId, queryDTO.getStatus());
        return PageResult.of(result);
    }

    @Override
    public WorkOrderDetailVO getWorkOrderDetail(Long id, Long ownerId) {
        WorkOrderDO order = workOrderMapper.selectById(id);
        if (order == null || !ownerId.equals(order.getReporterId())) {
            throw new BusinessException(WorkOrderErrorCode.WORK_ORDER_NOT_YOURS.getCode(),
                    WorkOrderErrorCode.WORK_ORDER_NOT_YOURS.getMessage());
        }
        return queryService.getWorkOrderDetail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyWorkOrder(Long id, WorkOrderVerifyDTO verifyDTO, Long ownerId) {
        WorkOrderDO order = getAndCheckOwner(id, ownerId);
        if (!WorkOrderStatus.PENDING_VERIFY.getCode().equals(order.getStatus())) {
            throw new BusinessException(WorkOrderErrorCode.INVALID_STATUS_TRANSITION.getCode(), "工单状态不是待验收");
        }

        String fromStatus = order.getStatus();

        if (Boolean.TRUE.equals(verifyDTO.getPassed())) {
            stateMachine.checkTransition(WorkOrderStatus.PENDING_VERIFY, WorkOrderStatus.COMPLETED);
            order.setStatus(WorkOrderStatus.COMPLETED.getCode());
            workOrderMapper.updateById(order);
            saveLog(id, WorkOrderAction.VERIFY_PASS, fromStatus,
                    WorkOrderStatus.COMPLETED.getCode(), ownerId, "业主验收通过");
        } else {
            stateMachine.checkTransition(WorkOrderStatus.PENDING_VERIFY, WorkOrderStatus.IN_PROGRESS);
            if (verifyDTO.getRejectReason() == null || verifyDTO.getRejectReason().isEmpty()) {
                throw new BusinessException(WorkOrderErrorCode.VERIFY_REJECT_REASON_REQUIRED.getCode(),
                        WorkOrderErrorCode.VERIFY_REJECT_REASON_REQUIRED.getMessage());
            }

            int newRejectCount = order.getRejectCount() + 1;
            if (newRejectCount > WorkOrderConstants.MAX_REJECT_COUNT) {
                throw new BusinessException(WorkOrderErrorCode.REJECT_LIMIT_EXCEEDED.getCode(),
                        WorkOrderErrorCode.REJECT_LIMIT_EXCEEDED.getMessage());
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

            eventPublisher.publishVerifyReject(id, order.getRepairUserId(),
                    order.getOrderNo(), newRejectCount);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelWorkOrder(Long id, WorkOrderCancelDTO cancelDTO, Long ownerId) {
        WorkOrderDO order = getAndCheckOwner(id, ownerId);
        stateMachine.checkTransition(
                WorkOrderStatus.fromCode(order.getStatus()),
                WorkOrderStatus.CANCELLED
        );

        String fromStatus = order.getStatus();
        order.setStatus(WorkOrderStatus.CANCELLED.getCode());
        order.setCancelReason(cancelDTO.getCancelReason());
        order.setCancelBy(ownerId);
        workOrderMapper.updateById(order);

        saveLog(id, WorkOrderAction.CANCEL, fromStatus,
                WorkOrderStatus.CANCELLED.getCode(), ownerId, cancelDTO.getCancelReason());

        if (order.getRepairUserId() != null) {
            eventPublisher.publishCancel(id, order.getRepairUserId(), order.getOrderNo());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void urgeWorkOrder(Long id, Long ownerId) {
        WorkOrderDO order = getAndCheckOwner(id, ownerId);

        int newUrgeCount = order.getUrgeCount() + 1;
        order.setUrgeCount(newUrgeCount);
        order.setLastUrgeTime(LocalDateTime.now());
        workOrderMapper.updateById(order);

        saveLog(id, WorkOrderAction.URGE, order.getStatus(),
                order.getStatus(), ownerId, "业主催单（第" + newUrgeCount + "次）");

        if (order.getRepairUserId() != null) {
            eventPublisher.publishUrge(id, order.getRepairUserId(), order.getOrderNo());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void evaluateWorkOrder(Long id, WorkOrderEvaluateDTO evaluateDTO, Long ownerId) {
        WorkOrderDO order = getAndCheckOwner(id, ownerId);
        if (!WorkOrderStatus.COMPLETED.getCode().equals(order.getStatus())) {
            throw new BusinessException(WorkOrderErrorCode.INVALID_STATUS_TRANSITION.getCode(), "只有已完成的工单可以评价");
        }

        EvaluationDO existing = evaluationMapper.selectByOrderId(id);
        if (existing != null) {
            throw new BusinessException(WorkOrderErrorCode.EVALUATION_ALREADY_EXISTS.getCode(),
                    WorkOrderErrorCode.EVALUATION_ALREADY_EXISTS.getMessage());
        }

        EvaluationDO evaluation = new EvaluationDO();
        evaluation.setOrderId(id);
        evaluation.setScore(evaluateDTO.getScore());
        evaluation.setContent(evaluateDTO.getContent());
        evaluation.setEvaluatorId(ownerId);
        evaluation.setEvaluateType(EvaluateType.OWNER.getCode());
        evaluationMapper.insert(evaluation);

        saveLog(id, WorkOrderAction.VERIFY_PASS, WorkOrderStatus.COMPLETED.getCode(),
                WorkOrderStatus.COMPLETED.getCode(), ownerId, "业主评价：" + evaluateDTO.getScore() + "星");
    }

    private WorkOrderDO getAndCheckOwner(Long id, Long ownerId) {
        WorkOrderDO order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(WorkOrderErrorCode.WORK_ORDER_NOT_FOUND.getCode(),
                    WorkOrderErrorCode.WORK_ORDER_NOT_FOUND.getMessage());
        }
        if (!ownerId.equals(order.getReporterId())) {
            throw new BusinessException(WorkOrderErrorCode.WORK_ORDER_NOT_YOURS.getCode(),
                    WorkOrderErrorCode.WORK_ORDER_NOT_YOURS.getMessage());
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
        logDO.setOperatorType("OWNER");
        logDO.setRemark(remark);
        logDO.setOperateTime(LocalDateTime.now());
        workOrderLogMapper.insert(logDO);
    }
}
