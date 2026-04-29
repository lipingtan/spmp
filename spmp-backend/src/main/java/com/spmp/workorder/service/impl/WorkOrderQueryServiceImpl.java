package com.spmp.workorder.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spmp.common.exception.BusinessException;
import com.spmp.common.result.PageResult;
import com.spmp.workorder.constant.WorkOrderErrorCode;
import com.spmp.workorder.domain.dto.WorkOrderQueryDTO;
import com.spmp.workorder.domain.entity.*;
import com.spmp.workorder.domain.vo.*;
import com.spmp.workorder.repository.*;
import com.spmp.workorder.service.WorkOrderQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 工单查询服务实现。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkOrderQueryServiceImpl implements WorkOrderQueryService {

    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderImageMapper workOrderImageMapper;
    private final DispatchRecordMapper dispatchRecordMapper;
    private final RepairMaterialMapper repairMaterialMapper;
    private final EvaluationMapper evaluationMapper;
    private final WorkOrderLogMapper workOrderLogMapper;

    @Override
    public PageResult<WorkOrderListVO> listWorkOrders(WorkOrderQueryDTO queryDTO) {
        IPage<WorkOrderListVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<WorkOrderListVO> result = workOrderMapper.selectWorkOrderPage(page, queryDTO);
        return PageResult.of(result);
    }

    @Override
    public WorkOrderDetailVO getWorkOrderDetail(Long id) {
        WorkOrderDO order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(WorkOrderErrorCode.WORK_ORDER_NOT_FOUND.getCode(),
                    WorkOrderErrorCode.WORK_ORDER_NOT_FOUND.getMessage());
        }

        WorkOrderDetailVO detail = new WorkOrderDetailVO();
        detail.setId(order.getId());
        detail.setOrderNo(order.getOrderNo());
        detail.setOrderType(order.getOrderType());
        detail.setAddressType(order.getAddressType());
        detail.setCommunityId(order.getCommunityId());
        detail.setHouseId(order.getHouseId());
        detail.setBuildingId(order.getBuildingId());
        detail.setUnitId(order.getUnitId());
        detail.setReporterId(order.getReporterId());
        detail.setReporterName(order.getReporterName());
        detail.setDescription(order.getDescription());
        detail.setStatus(order.getStatus());
        detail.setRepairUserId(order.getRepairUserId());
        detail.setRejectCount(order.getRejectCount());
        detail.setUrgeCount(order.getUrgeCount());
        detail.setLastUrgeTime(order.getLastUrgeTime());
        detail.setExpectedCompleteTime(order.getExpectedCompleteTime());
        detail.setActualStartTime(order.getActualStartTime());
        detail.setActualCompleteTime(order.getActualCompleteTime());
        detail.setRepairDuration(order.getRepairDuration());
        detail.setCancelReason(order.getCancelReason());
        detail.setCreateTime(toLocalDateTime(order.getCreateTime()));

        List<WorkOrderImageDO> reportImages = workOrderImageMapper.selectByOrderIdAndType(id, "REPORT");
        detail.setReportImages(reportImages.stream().map(WorkOrderImageDO::getImageUrl).collect(Collectors.toList()));

        List<DispatchRecordDO> dispatchRecords = dispatchRecordMapper.selectByOrderId(id);
        detail.setDispatchRecords(dispatchRecords.stream().map(this::toDispatchRecordVO).collect(Collectors.toList()));

        List<RepairMaterialDO> materials = repairMaterialMapper.selectByOrderId(id);
        detail.setMaterials(materials.stream().map(this::toRepairMaterialVO).collect(Collectors.toList()));

        EvaluationDO evaluation = evaluationMapper.selectByOrderId(id);
        if (evaluation != null) {
            detail.setEvaluation(toEvaluationVO(evaluation));
        }

        List<WorkOrderLogDO> logs = workOrderLogMapper.selectByOrderId(id);
        detail.setLogs(logs.stream().map(this::toWorkOrderLogVO).collect(Collectors.toList()));

        return detail;
    }

    private DispatchRecordVO toDispatchRecordVO(DispatchRecordDO record) {
        DispatchRecordVO vo = new DispatchRecordVO();
        vo.setId(record.getId());
        vo.setRepairUserId(record.getRepairUserId());
        vo.setRepairUserName(record.getRepairUserName());
        vo.setDispatchType(record.getDispatchType());
        vo.setDispatcherName(record.getDispatcherName());
        vo.setRemark(record.getRemark());
        vo.setDispatchTime(record.getDispatchTime());
        return vo;
    }

    private RepairMaterialVO toRepairMaterialVO(RepairMaterialDO material) {
        RepairMaterialVO vo = new RepairMaterialVO();
        vo.setId(material.getId());
        vo.setMaterialName(material.getMaterialName());
        vo.setQuantity(material.getQuantity());
        vo.setUnit(material.getUnit());
        vo.setUnitPrice(material.getUnitPrice());
        vo.setTotalPrice(material.getTotalPrice());
        return vo;
    }

    private EvaluationVO toEvaluationVO(EvaluationDO evaluation) {
        EvaluationVO vo = new EvaluationVO();
        vo.setId(evaluation.getId());
        vo.setScore(evaluation.getScore());
        vo.setContent(evaluation.getContent());
        vo.setEvaluatorId(evaluation.getEvaluatorId());
        vo.setEvaluateTime(evaluation.getEvaluateTime());
        vo.setEvaluateType(evaluation.getEvaluateType());
        return vo;
    }

    private WorkOrderLogVO toWorkOrderLogVO(WorkOrderLogDO logDO) {
        WorkOrderLogVO vo = new WorkOrderLogVO();
        vo.setId(logDO.getId());
        vo.setAction(logDO.getAction());
        vo.setFromStatus(logDO.getFromStatus());
        vo.setToStatus(logDO.getToStatus());
        vo.setOperatorId(logDO.getOperatorId());
        vo.setOperatorName(logDO.getOperatorName());
        vo.setOperatorType(logDO.getOperatorType());
        vo.setRemark(logDO.getRemark());
        vo.setOperateTime(logDO.getOperateTime());
        return vo;
    }

    private LocalDateTime toLocalDateTime(java.util.Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
