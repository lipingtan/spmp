package com.spmp.billing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spmp.billing.constant.BillStatus;
import com.spmp.billing.constant.BillingErrorCode;
import com.spmp.billing.domain.converter.BillingConverter;
import com.spmp.billing.domain.dto.H5BillQueryDTO;
import com.spmp.billing.domain.dto.H5PaymentCreateDTO;
import com.spmp.billing.domain.entity.BillDO;
import com.spmp.billing.domain.entity.PaymentBillDO;
import com.spmp.billing.domain.entity.ReduceRecordDO;
import com.spmp.billing.domain.vo.H5BillDetailVO;
import com.spmp.billing.domain.vo.H5BillListVO;
import com.spmp.billing.domain.vo.PaymentVO;
import com.spmp.billing.domain.vo.ReduceRecordVO;
import com.spmp.billing.repository.BillMapper;
import com.spmp.billing.repository.PaymentBillMapper;
import com.spmp.billing.repository.PaymentMapper;
import com.spmp.billing.repository.ReduceRecordMapper;
import com.spmp.billing.service.H5BillService;
import com.spmp.billing.service.PaymentService;
import com.spmp.common.exception.BusinessException;
import com.spmp.common.result.PageResult;
import com.spmp.common.util.SecurityUtils;
import com.spmp.owner.domain.entity.OwnerDO;
import com.spmp.owner.repository.OwnerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * H5 业主端缴费服务实现。
 * <p>
 * 封装 H5 端账单查询、支付创建、支付回调等业务逻辑。
 * Controller 层只依赖本 Service，不直接操作 Mapper。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class H5BillServiceImpl implements H5BillService {

    private final BillMapper billMapper;
    private final PaymentMapper paymentMapper;
    private final PaymentBillMapper paymentBillMapper;
    private final ReduceRecordMapper reduceRecordMapper;
    private final OwnerMapper ownerMapper;
    private final PaymentService paymentService;

    // ----------------------------------------------------------------
    // 业主身份解析
    // ----------------------------------------------------------------

    @Override
    public Long getCurrentOwnerId() {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<OwnerDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OwnerDO::getUserId, userId)
                .eq(OwnerDO::getDelFlag, 0)
                .last("LIMIT 1");
        OwnerDO owner = ownerMapper.selectOne(wrapper);
        if (owner == null) {
            throw new BusinessException(BillingErrorCode.BILL_NOT_FOUND.getCode(), "当前用户未绑定业主信息");
        }
        return owner.getId();
    }

    // ----------------------------------------------------------------
    // 账单查询
    // ----------------------------------------------------------------

    @Override
    public PageResult<H5BillListVO> listMyBills(Long ownerId, H5BillQueryDTO queryDTO) {
        LambdaQueryWrapper<BillDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BillDO::getOwnerId, ownerId)
                .eq(queryDTO.getStatus() != null, BillDO::getStatus, queryDTO.getStatus())
                .eq(queryDTO.getFeeType() != null, BillDO::getFeeType, queryDTO.getFeeType())
                .eq(BillDO::getDelFlag, 0)
                .orderByDesc(BillDO::getCreateTime);
        Page<BillDO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<BillDO> billPage = billMapper.selectPage(page, wrapper);

        PageResult<H5BillListVO> result = new PageResult<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(billPage.getRecords().stream()
                .map(this::toH5BillListVO)
                .collect(Collectors.toList()));
        result.setTotal(billPage.getTotal());
        result.setPageNum((int) billPage.getCurrent());
        result.setPageSize((int) billPage.getSize());
        return result;
    }

    @Override
    public H5BillDetailVO getBillDetail(Long id, Long ownerId) {
        BillDO bill = billMapper.selectById(id);
        if (bill == null) {
            throw new BusinessException(BillingErrorCode.BILL_NOT_FOUND.getCode(),
                    BillingErrorCode.BILL_NOT_FOUND.getMessage());
        }
        // 归属权校验：只能查看自己的账单
        if (!bill.getOwnerId().equals(ownerId)) {
            throw new BusinessException(BillingErrorCode.BILL_NOT_YOURS.getCode(),
                    BillingErrorCode.BILL_NOT_YOURS.getMessage());
        }

        H5BillDetailVO vo = buildH5BillDetailVO(bill);

        // 查询关联支付记录
        LambdaQueryWrapper<PaymentBillDO> pbWrapper = new LambdaQueryWrapper<>();
        pbWrapper.eq(PaymentBillDO::getBillId, id);
        List<PaymentVO> payments = paymentBillMapper.selectList(pbWrapper).stream()
                .map(pb -> paymentMapper.selectById(pb.getPaymentId()))
                .filter(p -> p != null)
                .map(BillingConverter::toPaymentVO)
                .collect(Collectors.toList());
        vo.setPayments(payments);

        // 查询已审批的减免记录
        LambdaQueryWrapper<ReduceRecordDO> reduceWrapper = new LambdaQueryWrapper<>();
        reduceWrapper.eq(ReduceRecordDO::getBillId, id)
                .eq(ReduceRecordDO::getStatus, "APPROVED")
                .orderByDesc(ReduceRecordDO::getApproveTime);
        List<ReduceRecordVO> reduceRecords = reduceRecordMapper.selectList(reduceWrapper).stream()
                .map(BillingConverter::toReduceRecordVO)
                .collect(Collectors.toList());
        vo.setReduceRecords(reduceRecords);

        return vo;
    }

    // ----------------------------------------------------------------
    // 支付
    // ----------------------------------------------------------------

    @Override
    public Long createPayment(H5PaymentCreateDTO dto, Long ownerId) {
        return paymentService.createPayment(dto, ownerId);
    }

    @Override
    public void paymentCallback(Long paymentId, String callbackSign) {
        paymentService.paymentCallback(paymentId, callbackSign);
    }

    @Override
    public PaymentVO getPaymentDetail(Long id, Long ownerId) {
        return paymentService.getPaymentDetail(id, ownerId);
    }

    @Override
    public PageResult<PaymentVO> listPaymentHistory(Long ownerId, Integer pageNum, Integer pageSize) {
        IPage<PaymentVO> page = paymentService.listPaymentHistory(ownerId, pageNum, pageSize);
        PageResult<PaymentVO> result = new PageResult<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(page.getRecords());
        result.setTotal(page.getTotal());
        result.setPageNum((int) page.getCurrent());
        result.setPageSize((int) page.getSize());
        return result;
    }

    // ----------------------------------------------------------------
    // 私有转换方法
    // ----------------------------------------------------------------

    /**
     * 构建 H5 账单详情 VO（基础字段，不含关联记录）。
     */
    private H5BillDetailVO buildH5BillDetailVO(BillDO bill) {
        H5BillDetailVO vo = new H5BillDetailVO();
        vo.setId(bill.getId());
        vo.setBillNo(bill.getBillNo());
        vo.setFeeType(bill.getFeeType());
        vo.setFeeTypeName(BillingConverter.feeTypeName(bill.getFeeType()));
        BigDecimal amount = bill.getAmount() != null ? bill.getAmount() : BigDecimal.ZERO;
        BigDecimal reduceAmount = bill.getReduceAmount() != null ? bill.getReduceAmount() : BigDecimal.ZERO;
        vo.setAmount(amount);
        vo.setReduceAmount(reduceAmount);
        vo.setPayAmount(BillingConverter.calcPayAmount(bill.getAmount(), bill.getReduceAmount()));
        vo.setStatus(bill.getStatus());
        vo.setStatusName(BillingConverter.billStatusName(bill.getStatus()));
        // TODO: 通过 BaseApi 查询房屋全称（小区-楼栋-单元-房号），待 BaseApi 集成后替换
        vo.setHouseFullName(null);
        vo.setBillingPeriod(bill.getBillingPeriod());
        vo.setDueDate(bill.getDueDate());
        vo.setUsageAmount(bill.getUsageAmount());
        vo.setUnitPrice(bill.getUnitPrice());
        vo.setHouseArea(bill.getHouseArea());
        vo.setPaidTime(bill.getPaidTime());
        vo.setCreateTime(bill.getCreateTime());
        vo.setRemark(bill.getRemark());
        return vo;
    }

    /**
     * BillDO → H5BillListVO。
     */
    private H5BillListVO toH5BillListVO(BillDO bill) {
        H5BillListVO vo = new H5BillListVO();
        vo.setId(bill.getId());
        vo.setBillNo(bill.getBillNo());
        vo.setFeeType(bill.getFeeType());
        vo.setFeeTypeName(BillingConverter.feeTypeName(bill.getFeeType()));
        BigDecimal payAmount = BillingConverter.calcPayAmount(bill.getAmount(), bill.getReduceAmount());
        vo.setAmount(bill.getAmount() != null ? bill.getAmount() : BigDecimal.ZERO);
        vo.setReduceAmount(bill.getReduceAmount() != null ? bill.getReduceAmount() : BigDecimal.ZERO);
        vo.setPayAmount(payAmount);
        vo.setStatus(bill.getStatus());
        vo.setStatusName(BillingConverter.billStatusName(bill.getStatus()));
        vo.setBillingPeriod(bill.getBillingPeriod());
        vo.setDueDate(bill.getDueDate());
        vo.setOverdue(BillStatus.OVERDUE.getCode().equals(bill.getStatus()));
        return vo;
    }

    // VO 转换统一由 BillingConverter 工具类处理，见 domain/converter/BillingConverter.java
}
