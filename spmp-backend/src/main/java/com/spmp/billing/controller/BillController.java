package com.spmp.billing.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spmp.billing.constant.BillStatus;
import com.spmp.billing.constant.BillingErrorCode;
import com.spmp.billing.domain.converter.BillingConverter;
import com.spmp.billing.domain.dto.BillCancelDTO;
import com.spmp.billing.domain.dto.BillCreateDTO;
import com.spmp.billing.domain.dto.BillGenerateDTO;
import com.spmp.billing.domain.dto.BillQueryDTO;
import com.spmp.billing.domain.dto.BillReduceApproveDTO;
import com.spmp.billing.domain.dto.BillReduceDTO;
import com.spmp.billing.domain.dto.BillRefundDTO;
import com.spmp.billing.domain.entity.BillDO;
import com.spmp.billing.domain.entity.ReduceRecordDO;
import com.spmp.billing.domain.entity.RefundRecordDO;
import com.spmp.billing.domain.vo.BillDetailVO;
import com.spmp.billing.domain.vo.BillGeneratePreviewVO;
import com.spmp.billing.domain.vo.BillGenerateResultVO;
import com.spmp.billing.domain.vo.BillListVO;
import com.spmp.billing.domain.vo.ReduceRecordVO;
import com.spmp.billing.domain.vo.RefundRecordVO;
import com.spmp.billing.domain.vo.UrgeRecordVO;
import com.spmp.billing.repository.BillMapper;
import com.spmp.billing.repository.BillUrgeRecordMapper;
import com.spmp.billing.repository.ReduceRecordMapper;
import com.spmp.billing.repository.RefundRecordMapper;
import com.spmp.base.domain.entity.BuildingDO;
import com.spmp.base.domain.entity.HouseDO;
import com.spmp.base.domain.entity.UnitDO;
import com.spmp.base.repository.BuildingMapper;
import com.spmp.base.repository.HouseMapper;
import com.spmp.base.repository.UnitMapper;
import com.spmp.common.exception.BusinessException;
import com.spmp.common.result.PageResult;
import com.spmp.common.result.Result;
import com.spmp.common.util.RedisUtils;
import com.spmp.common.util.SecurityUtils;
import com.spmp.owner.domain.entity.OwnerDO;
import com.spmp.owner.domain.entity.PropertyBindingDO;
import com.spmp.owner.repository.OwnerMapper;
import com.spmp.owner.repository.PropertyBindingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 账单管理 Controller（PC）。
 */
@RestController
@RequestMapping("/api/v1/billing/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillMapper billMapper;
    private final ReduceRecordMapper reduceRecordMapper;
    private final RefundRecordMapper refundRecordMapper;
    private final BillUrgeRecordMapper urgeRecordMapper;
    private final OwnerMapper ownerMapper;
    private final HouseMapper houseMapper;
    private final UnitMapper unitMapper;
    private final BuildingMapper buildingMapper;
    private final PropertyBindingMapper propertyBindingMapper;
    private final RedisUtils redisUtils;

    @GetMapping
    public Result<PageResult<BillListVO>> list(BillQueryDTO queryDTO) {
        long pageNum = queryDTO.getPageNum() == null ? 1L : queryDTO.getPageNum();
        long pageSize = queryDTO.getPageSize() == null ? 10L : queryDTO.getPageSize();
        IPage<BillListVO> page = billMapper.selectBillPage(new Page<>(pageNum, pageSize), queryDTO);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<BillDetailVO> detail(@PathVariable Long id) {
        BillDO bill = getBillOrThrow(id);
        BillDetailVO vo = new BillDetailVO();
        vo.setId(bill.getId());
        vo.setBillNo(bill.getBillNo());
        vo.setBatchNo(bill.getBatchNo());
        vo.setFeeType(bill.getFeeType());
        vo.setFeeTypeName(BillingConverter.feeTypeName(bill.getFeeType()));
        vo.setAmount(bill.getAmount());
        vo.setReduceAmount(bill.getReduceAmount());
        vo.setPaidAmount(bill.getPaidAmount());
        vo.setRefundAmount(bill.getRefundAmount());
        vo.setStatus(bill.getStatus());
        vo.setStatusName(BillingConverter.billStatusName(bill.getStatus()));
        vo.setOwnerId(bill.getOwnerId());
        vo.setOwnerName(bill.getOwnerName());
        vo.setHouseId(bill.getHouseId());
        vo.setCommunityId(bill.getCommunityId());
        vo.setBuildingId(bill.getBuildingId());
        vo.setBillingPeriod(bill.getBillingPeriod());
        vo.setDueDate(bill.getDueDate());
        vo.setUsageAmount(bill.getUsageAmount());
        vo.setUnitPrice(bill.getUnitPrice());
        vo.setHouseArea(bill.getHouseArea());
        vo.setPaidTime(bill.getPaidTime());
        vo.setRemark(bill.getRemark());
        vo.setCreateTime(bill.getCreateTime());
        List<ReduceRecordVO> reduceRecords = reduceRecordMapper.selectList(
                        new LambdaQueryWrapper<ReduceRecordDO>().eq(ReduceRecordDO::getBillId, id).orderByDesc(ReduceRecordDO::getCreateTime))
                .stream().map(BillingConverter::toReduceRecordVO).collect(Collectors.toList());
        List<RefundRecordVO> refundRecords = refundRecordMapper.selectByBillId(id).stream()
                .map(BillingConverter::toRefundRecordVO).collect(Collectors.toList());
        List<UrgeRecordVO> urgeRecords = urgeRecordMapper.selectByBillId(id).stream()
                .map(BillingConverter::toUrgeRecordVO).collect(Collectors.toList());
        vo.setReduceRecords(reduceRecords);
        vo.setRefundRecords(refundRecords);
        vo.setUrgeRecords(urgeRecords);
        return Result.success(vo);
    }

    @PostMapping("/generate/preview")
    public Result<BillGeneratePreviewVO> generatePreview(@Valid @RequestBody BillGenerateDTO dto) {
        BillGeneratePreviewVO preview = new BillGeneratePreviewVO();
        preview.setTotalHouses(1);
        preview.setWillGenerate(1);
        preview.setSkippedVacant(0);
        preview.setSkippedDisabled(0);
        preview.setSkippedNoConfig(0);
        preview.setTotalAmount(BigDecimal.ZERO);
        return Result.success(preview);
    }

    @PostMapping("/generate")
    public Result<String> generate(@Valid @RequestBody BillGenerateDTO dto) {
        String batchNo = "BATCH" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        BillGenerateResultVO progress = new BillGenerateResultVO();
        progress.setBatchNo(batchNo);
        progress.setTotal(0);
        progress.setSuccess(0);
        progress.setSkipped(0);
        progress.setFailed(0);
        progress.setStatus("COMPLETED");
        redisUtils.set("billing:generate:" + batchNo, progress, 1, TimeUnit.HOURS);
        return Result.success(batchNo);
    }

    @GetMapping("/generate/progress/{batchNo}")
    public Result<BillGenerateResultVO> generateProgress(@PathVariable String batchNo) {
        BillGenerateResultVO vo = redisUtils.get("billing:generate:" + batchNo, BillGenerateResultVO.class);
        if (vo == null) {
            vo = new BillGenerateResultVO();
            vo.setBatchNo(batchNo);
            vo.setTotal(0);
            vo.setSuccess(0);
            vo.setSkipped(0);
            vo.setFailed(0);
            vo.setStatus("COMPLETED");
        }
        return Result.success(vo);
    }

    @PostMapping("/regenerate/{batchNo}")
    public Result<String> regenerate(@PathVariable String batchNo, @Valid @RequestBody BillGenerateDTO dto) {
        return generate(dto);
    }

    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> create(@Valid @RequestBody BillCreateDTO dto) {
        OwnerDO owner = ownerMapper.selectById(dto.getOwnerId());
        if (owner == null) {
            throw new BusinessException(BillingErrorCode.BILL_NOT_FOUND.getCode(), "业主不存在");
        }
        HouseDO house = houseMapper.selectById(dto.getHouseId());
        if (house == null) {
            throw new BusinessException(BillingErrorCode.BILL_NOT_FOUND.getCode(), "房屋不存在");
        }
        PropertyBindingDO binding = propertyBindingMapper.selectOne(new LambdaQueryWrapper<PropertyBindingDO>()
                .eq(PropertyBindingDO::getOwnerId, dto.getOwnerId())
                .eq(PropertyBindingDO::getHouseId, dto.getHouseId())
                .eq(PropertyBindingDO::getStatus, 0)
                .last("limit 1"));
        if (binding == null) {
            throw new BusinessException(BillingErrorCode.HOUSE_NO_OWNER.getCode(), "所选房屋未绑定该业主");
        }
        UnitDO unit = unitMapper.selectById(house.getUnitId());
        if (unit == null) {
            throw new BusinessException(BillingErrorCode.BILL_NOT_FOUND.getCode(), "房屋所属单元不存在");
        }
        BuildingDO building = buildingMapper.selectById(unit.getBuildingId());
        if (building == null) {
            throw new BusinessException(BillingErrorCode.BILL_NOT_FOUND.getCode(), "房屋所属楼栋不存在");
        }
        if (dto.getCommunityId() != null && !dto.getCommunityId().equals(building.getCommunityId())) {
            throw new BusinessException(BillingErrorCode.BILL_NOT_FOUND.getCode(), "小区与房屋不匹配");
        }
        if (dto.getBuildingId() != null && !dto.getBuildingId().equals(building.getId())) {
            throw new BusinessException(BillingErrorCode.BILL_NOT_FOUND.getCode(), "楼栋与房屋不匹配");
        }

        BillDO bill = new BillDO();
        bill.setBillNo(genBillNo());
        bill.setBatchNo(null);
        bill.setFeeType(dto.getFeeType());
        bill.setAmount(dto.getAmount());
        bill.setReduceAmount(BigDecimal.ZERO);
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setRefundAmount(BigDecimal.ZERO);
        bill.setStatus(BillStatus.UNPAID.getCode());
        bill.setOwnerId(dto.getOwnerId());
        bill.setOwnerName(owner.getOwnerName());
        bill.setHouseId(dto.getHouseId());
        bill.setBuildingId(building.getId());
        bill.setCommunityId(building.getCommunityId());
        bill.setBillingPeriod(dto.getBillingPeriod());
        bill.setDueDate(dto.getDueDate() == null ? LocalDate.now().plusDays(15) : dto.getDueDate());
        bill.setUsageAmount(dto.getUsageAmount());
        bill.setUnitPrice(dto.getUnitPrice());
        bill.setHouseArea(dto.getHouseArea());
        bill.setReduceApproved((byte) 0);
        bill.setRemark(dto.getRemark());
        billMapper.insert(bill);
        return Result.success(bill.getId());
    }

    @PostMapping("/{id}/reduce")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> applyReduce(@PathVariable Long id, @Valid @RequestBody BillReduceDTO dto) {
        BillDO bill = getBillOrThrow(id);
        if (!BillStatus.UNPAID.getCode().equals(bill.getStatus()) && !BillStatus.OVERDUE.getCode().equals(bill.getStatus())) {
            throw new BusinessException(BillingErrorCode.INVALID_STATUS_TRANSITION.getCode(), BillingErrorCode.INVALID_STATUS_TRANSITION.getMessage());
        }
        ReduceRecordDO record = new ReduceRecordDO();
        record.setBillId(id);
        record.setBillNo(bill.getBillNo());
        record.setReduceAmount(dto.getReduceAmount());
        record.setReason(dto.getReason());
        record.setStatus("PENDING");
        record.setApplicantId(SecurityUtils.getCurrentUserId());
        record.setApplicantName("SYSTEM");
        reduceRecordMapper.insert(record);
        return Result.success(null);
    }

    @PutMapping("/{id}/reduce/approve")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> approveReduce(@PathVariable Long id, @Valid @RequestBody BillReduceApproveDTO dto) {
        ReduceRecordDO record = reduceRecordMapper.selectOne(new LambdaQueryWrapper<ReduceRecordDO>()
                .eq(ReduceRecordDO::getBillId, id)
                .eq(ReduceRecordDO::getStatus, "PENDING")
                .orderByDesc(ReduceRecordDO::getCreateTime)
                .last("limit 1"));
        if (record == null) {
            throw new BusinessException(BillingErrorCode.REDUCE_NOT_PENDING.getCode(), BillingErrorCode.REDUCE_NOT_PENDING.getMessage());
        }
        record.setStatus(Boolean.TRUE.equals(dto.getApproved()) ? "APPROVED" : "REJECTED");
        record.setApproverId(SecurityUtils.getCurrentUserId());
        record.setApproverName("SYSTEM");
        record.setApproveTime(LocalDateTime.now());
        record.setApproveRemark(dto.getApproveRemark());
        reduceRecordMapper.updateById(record);
        if (Boolean.TRUE.equals(dto.getApproved())) {
            BillDO bill = getBillOrThrow(id);
            BigDecimal newReduce = (bill.getReduceAmount() == null ? BigDecimal.ZERO : bill.getReduceAmount()).add(record.getReduceAmount());
            bill.setReduceAmount(newReduce);
            if ((bill.getAmount() == null ? BigDecimal.ZERO : bill.getAmount()).compareTo(newReduce) <= 0) {
                bill.setStatus(BillStatus.REDUCED.getCode());
            }
            billMapper.updateById(bill);
        }
        return Result.success(null);
    }

    @PutMapping("/{id}/reduce/revoke")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> revokeReduce(@PathVariable Long id) {
        ReduceRecordDO record = reduceRecordMapper.selectOne(new LambdaQueryWrapper<ReduceRecordDO>()
                .eq(ReduceRecordDO::getBillId, id)
                .eq(ReduceRecordDO::getStatus, "PENDING")
                .orderByDesc(ReduceRecordDO::getCreateTime)
                .last("limit 1"));
        if (record != null) {
            record.setStatus("REVOKED");
            reduceRecordMapper.updateById(record);
        }
        return Result.success(null);
    }

    @PostMapping("/{id}/refund")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> refund(@PathVariable Long id, @Valid @RequestBody BillRefundDTO dto) {
        BillDO bill = getBillOrThrow(id);
        if (!BillStatus.PAID.getCode().equals(bill.getStatus())) {
            throw new BusinessException(BillingErrorCode.INVALID_STATUS_TRANSITION.getCode(), BillingErrorCode.INVALID_STATUS_TRANSITION.getMessage());
        }
        BigDecimal paidAmount = bill.getPaidAmount() == null ? BigDecimal.ZERO : bill.getPaidAmount();
        BigDecimal refundedAmount = bill.getRefundAmount() == null ? BigDecimal.ZERO : bill.getRefundAmount();
        BigDecimal refundable = paidAmount.subtract(refundedAmount);
        if (dto.getRefundAmount().compareTo(refundable) > 0) {
            throw new BusinessException(BillingErrorCode.REFUND_AMOUNT_EXCEED.getCode(), BillingErrorCode.REFUND_AMOUNT_EXCEED.getMessage());
        }
        RefundRecordDO refund = new RefundRecordDO();
        refund.setBillId(id);
        refund.setBillNo(bill.getBillNo());
        refund.setRefundAmount(dto.getRefundAmount());
        refund.setRefundReason(dto.getRefundReason());
        Long currentUserId = SecurityUtils.getCurrentUserId();
        refund.setOperatorId(currentUserId == null ? 0L : currentUserId);
        refund.setOperatorName("SYSTEM");
        refund.setRefundTime(LocalDateTime.now());
        refund.setCreateTime(new Date());
        refundRecordMapper.insert(refund);
        BigDecimal newRefundAmount = refundedAmount.add(dto.getRefundAmount());
        bill.setRefundAmount(newRefundAmount);
        if (newRefundAmount.compareTo(paidAmount) >= 0) {
            bill.setStatus(BillStatus.REFUNDED.getCode());
        }
        billMapper.updateById(bill);
        return Result.success(null);
    }

    @PutMapping("/{id}/cancel")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> cancel(@PathVariable Long id, @Valid @RequestBody BillCancelDTO dto) {
        BillDO bill = getBillOrThrow(id);
        if (!BillStatus.UNPAID.getCode().equals(bill.getStatus()) && !BillStatus.OVERDUE.getCode().equals(bill.getStatus())) {
            throw new BusinessException(BillingErrorCode.INVALID_STATUS_TRANSITION.getCode(), BillingErrorCode.INVALID_STATUS_TRANSITION.getMessage());
        }
        bill.setStatus(BillStatus.CANCELLED.getCode());
        bill.setCancelReason(dto.getCancelReason());
        billMapper.updateById(bill);
        return Result.success(null);
    }

    @GetMapping("/export")
    public Result<List<BillListVO>> export(BillQueryDTO queryDTO) {
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10000);
        IPage<BillListVO> page = billMapper.selectBillPage(new Page<>(1, 10000), queryDTO);
        return Result.success(page.getRecords() == null ? Collections.emptyList() : page.getRecords());
    }

    private BillDO getBillOrThrow(Long id) {
        BillDO bill = billMapper.selectById(id);
        if (bill == null) {
            throw new BusinessException(BillingErrorCode.BILL_NOT_FOUND.getCode(), BillingErrorCode.BILL_NOT_FOUND.getMessage());
        }
        return bill;
    }

    private String genBillNo() {
        return "BL" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
