package com.spmp.billing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spmp.billing.constant.BillStatus;
import com.spmp.billing.constant.BillingErrorCode;
import com.spmp.billing.constant.PaymentStatus;
import com.spmp.billing.domain.converter.BillingConverter;
import com.spmp.billing.domain.dto.H5PaymentCreateDTO;
import com.spmp.billing.domain.entity.BillDO;
import com.spmp.billing.domain.entity.PaymentBillDO;
import com.spmp.billing.domain.entity.PaymentDO;
import com.spmp.billing.domain.vo.PaymentVO;
import com.spmp.billing.repository.BillMapper;
import com.spmp.billing.repository.PaymentBillMapper;
import com.spmp.billing.repository.PaymentMapper;
import com.spmp.common.sequence.config.SequenceProperties;
import com.spmp.common.sequence.constant.SequenceBizCode;
import com.spmp.common.sequence.service.SequenceService;
import com.spmp.billing.service.PaymentService;
import com.spmp.common.exception.BusinessException;
import com.spmp.common.exception.ErrorCode;
import com.spmp.common.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 支付服务实现。
 */
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final BillMapper billMapper;
    private final PaymentMapper paymentMapper;
    private final PaymentBillMapper paymentBillMapper;
    private final RedisUtils redisUtils;
    private final SequenceService sequenceService;
    private final SequenceProperties sequenceProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPayment(H5PaymentCreateDTO dto, Long ownerId) {
        List<BillDO> bills = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Long billId : dto.getBillIds()) {
            BillDO bill = billMapper.selectById(billId);
            if (bill == null || bill.getDelFlag() != null && bill.getDelFlag() == 1) {
                throw new BusinessException(BillingErrorCode.BILL_NOT_FOUND.getCode(), BillingErrorCode.BILL_NOT_FOUND.getMessage());
            }
            if (!ownerId.equals(bill.getOwnerId())) {
                throw new BusinessException(BillingErrorCode.BILL_NOT_YOURS.getCode(), BillingErrorCode.BILL_NOT_YOURS.getMessage());
            }
            if (!BillStatus.UNPAID.getCode().equals(bill.getStatus()) && !BillStatus.OVERDUE.getCode().equals(bill.getStatus())) {
                throw new BusinessException(BillingErrorCode.INVALID_STATUS_TRANSITION.getCode(), "当前账单状态不可发起支付");
            }
            bills.add(bill);
            total = total.add(BillingConverter.calcPayAmount(bill.getAmount(), bill.getReduceAmount()));
        }

        PaymentDO payment = new PaymentDO();
        payment.setPaymentNo(genPaymentNo());
        payment.setTotalAmount(total);
        payment.setPayMethod(dto.getPayMethod());
        payment.setStatus(PaymentStatus.PENDING.getCode());
        payment.setOwnerId(ownerId);
        payment.setOwnerName(bills.isEmpty() ? null : bills.get(0).getOwnerName());
        payment.setExpireTime(LocalDateTime.now().plusMinutes(30));
        paymentMapper.insert(payment);

        for (BillDO bill : bills) {
            PaymentBillDO paymentBill = new PaymentBillDO();
            paymentBill.setPaymentId(payment.getId());
            paymentBill.setBillId(bill.getId());
            paymentBill.setPayAmount(BillingConverter.calcPayAmount(bill.getAmount(), bill.getReduceAmount()));
            paymentBillMapper.insert(paymentBill);
            bill.setStatus(BillStatus.PAYING.getCode());
            billMapper.updateById(bill);
        }
        return payment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void paymentCallback(Long paymentId, String callbackSign) {
        PaymentDO payment = paymentMapper.selectById(paymentId);
        if (payment == null) {
            throw new BusinessException(BillingErrorCode.PAYMENT_NOT_FOUND.getCode(), BillingErrorCode.PAYMENT_NOT_FOUND.getMessage());
        }
        if (PaymentStatus.SUCCESS.getCode().equals(payment.getStatus())) {
            return;
        }
        if (!PaymentStatus.PENDING.getCode().equals(payment.getStatus())) {
            throw new BusinessException(BillingErrorCode.INVALID_PAYMENT_STATUS.getCode(), BillingErrorCode.INVALID_PAYMENT_STATUS.getMessage());
        }
        payment.setStatus(PaymentStatus.SUCCESS.getCode());
        payment.setPaidTime(LocalDateTime.now());
        paymentMapper.updateById(payment);

        List<PaymentBillDO> paymentBills = paymentBillMapper.selectList(
                new LambdaQueryWrapper<PaymentBillDO>().eq(PaymentBillDO::getPaymentId, paymentId));
        for (PaymentBillDO paymentBill : paymentBills) {
            BillDO bill = billMapper.selectById(paymentBill.getBillId());
            if (bill != null) {
                bill.setStatus(BillStatus.PAID.getCode());
                bill.setPaidAmount(paymentBill.getPayAmount());
                bill.setPaidTime(LocalDateTime.now());
                billMapper.updateById(bill);
            }
        }
    }

    @Override
    public PaymentVO getPaymentDetail(Long id, Long ownerId) {
        PaymentDO payment = paymentMapper.selectById(id);
        if (payment == null) {
            throw new BusinessException(BillingErrorCode.PAYMENT_NOT_FOUND.getCode(), BillingErrorCode.PAYMENT_NOT_FOUND.getMessage());
        }
        if (!ownerId.equals(payment.getOwnerId())) {
            throw new BusinessException(BillingErrorCode.PAYMENT_NOT_YOURS.getCode(), BillingErrorCode.PAYMENT_NOT_YOURS.getMessage());
        }
        return BillingConverter.toPaymentVO(payment);
    }

    @Override
    public IPage<PaymentVO> listPaymentHistory(Long ownerId, Integer pageNum, Integer pageSize) {
        Page<PaymentDO> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        LambdaQueryWrapper<PaymentDO> wrapper = new LambdaQueryWrapper<PaymentDO>()
                .eq(PaymentDO::getOwnerId, ownerId)
                .eq(PaymentDO::getStatus, PaymentStatus.SUCCESS.getCode())
                .orderByDesc(PaymentDO::getCreateTime);
        IPage<PaymentDO> paymentPage = paymentMapper.selectPage(page, wrapper);

        Page<PaymentVO> result = new Page<>(paymentPage.getCurrent(), paymentPage.getSize(), paymentPage.getTotal());
        List<PaymentVO> vos = new ArrayList<>();
        for (PaymentDO record : paymentPage.getRecords()) {
            vos.add(BillingConverter.toPaymentVO(record));
        }
        result.setRecords(vos);
        return result;
    }

    private String genPaymentNo() {
        String day = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        if ("segment".equalsIgnoreCase(sequenceProperties.getMode().getBilling())) {
            long seq = sequenceService.nextSequence(SequenceBizCode.PAYMENT, day);
            return "PAY" + day + String.format("%08d", seq);
        }
        Long seq = redisUtils.increment("billing:payment:seq:" + day, 1);
        if (seq == null || seq <= 0L) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED.getCode(), "支付单号生成失败，请稍后重试");
        }
        return "PAY" + day + String.format("%08d", seq);
    }
}
