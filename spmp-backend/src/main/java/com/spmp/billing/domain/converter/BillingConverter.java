package com.spmp.billing.domain.converter;

import com.spmp.billing.constant.BillStatus;
import com.spmp.billing.constant.FeeType;
import com.spmp.billing.constant.PayMethod;
import com.spmp.billing.constant.PaymentStatus;

import com.spmp.billing.domain.entity.PaymentDO;
import com.spmp.billing.domain.entity.ReduceRecordDO;
import com.spmp.billing.domain.entity.RefundRecordDO;
import com.spmp.billing.domain.entity.UrgeRecordDO;
import com.spmp.billing.domain.vo.PaymentVO;
import com.spmp.billing.domain.vo.ReduceRecordVO;
import com.spmp.billing.domain.vo.RefundRecordVO;
import com.spmp.billing.domain.vo.UrgeRecordVO;

import java.math.BigDecimal;

/**
 * 缴费模块 DO → VO 转换工具类（静态方法，无状态）。
 * <p>
 * 统一管理所有 DO 到 VO 的字段映射，避免转换逻辑散落在 Service 和 Controller 各处。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public final class BillingConverter {

    private BillingConverter() {
        // 工具类，禁止实例化
    }

    /**
     * 安全计算应付金额：amount - reduceAmount，任一为 null 时视为 0。
     *
     * @param amount       账单金额
     * @param reduceAmount 减免金额
     * @return 应付金额（非负）
     */
    public static BigDecimal calcPayAmount(BigDecimal amount, BigDecimal reduceAmount) {
        BigDecimal a = amount != null ? amount : BigDecimal.ZERO;
        BigDecimal r = reduceAmount != null ? reduceAmount : BigDecimal.ZERO;
        return a.subtract(r);
    }

    /**
     * 费用类型 code → 描述名称，找不到时返回原始 code。
     *
     * @param feeTypeCode 费用类型 code
     * @return 费用类型名称
     */
    public static String feeTypeName(String feeTypeCode) {
        FeeType feeType = FeeType.fromCode(feeTypeCode);
        return feeType != null ? feeType.getDescription() : feeTypeCode;
    }

    /**
     * 账单状态 code → 描述名称，找不到时返回原始 code。
     *
     * @param statusCode 账单状态 code
     * @return 账单状态名称
     */
    public static String billStatusName(String statusCode) {
        BillStatus status = BillStatus.fromCode(statusCode);
        return status != null ? status.getDescription() : statusCode;
    }

    /**
     * 支付状态 code → 描述名称，找不到时返回原始 code。
     *
     * @param statusCode 支付状态 code
     * @return 支付状态名称
     */
    public static String paymentStatusName(String statusCode) {
        PaymentStatus status = PaymentStatus.fromCode(statusCode);
        return status != null ? status.getDescription() : statusCode;
    }

    /**
     * PaymentDO → PaymentVO。
     *
     * @param payment 支付记录 DO
     * @return 支付记录 VO
     */
    public static PaymentVO toPaymentVO(PaymentDO payment) {
        if (payment == null) {
            return null;
        }
        PaymentVO vo = new PaymentVO();
        vo.setId(payment.getId());
        vo.setPaymentNo(payment.getPaymentNo());
        vo.setTotalAmount(payment.getTotalAmount());
        vo.setPayMethod(payment.getPayMethod());
        vo.setPayMethodName(payMethodName(payment.getPayMethod()));
        vo.setStatus(payment.getStatus());
        vo.setStatusName(paymentStatusName(payment.getStatus()));
        vo.setPaidTime(payment.getPaidTime());
        vo.setCreateTime(payment.getCreateTime());
        return vo;
    }

    /**
     * ReduceRecordDO → ReduceRecordVO。
     *
     * @param record 减免记录 DO
     * @return 减免记录 VO
     */
    public static ReduceRecordVO toReduceRecordVO(ReduceRecordDO record) {
        if (record == null) {
            return null;
        }
        ReduceRecordVO vo = new ReduceRecordVO();
        vo.setId(record.getId());
        vo.setBillNo(record.getBillNo());
        vo.setReduceAmount(record.getReduceAmount());
        vo.setReason(record.getReason());
        vo.setStatus(record.getStatus());
        vo.setApplicantName(record.getApplicantName());
        vo.setApproverName(record.getApproverName());
        vo.setApproveTime(record.getApproveTime());
        vo.setApproveRemark(record.getApproveRemark());
        vo.setCreateTime(record.getCreateTime());
        return vo;
    }

    /**
     * UrgeRecordDO → UrgeRecordVO。
     *
     * @param record 催收记录 DO
     * @return 催收记录 VO
     */
    public static UrgeRecordVO toUrgeRecordVO(UrgeRecordDO record) {
        if (record == null) {
            return null;
        }
        UrgeRecordVO vo = new UrgeRecordVO();
        vo.setId(record.getId());
        vo.setBillNo(record.getBillNo());
        vo.setUrgeType(record.getUrgeType());
        // UrgeType 枚举暂无 fromCode 方法，直接使用 code 作为名称
        vo.setUrgeTypeName(record.getUrgeType());
        vo.setUrgeUserName(record.getUrgeUserName());
        vo.setUrgeTime(record.getUrgeTime());
        vo.setFeedback(record.getFeedback());
        return vo;
    }

    /**
     * RefundRecordDO → RefundRecordVO。
     *
     * @param record 退款记录 DO
     * @return 退款记录 VO
     */
    public static RefundRecordVO toRefundRecordVO(RefundRecordDO record) {
        if (record == null) {
            return null;
        }
        RefundRecordVO vo = new RefundRecordVO();
        vo.setId(record.getId());
        vo.setBillNo(record.getBillNo());
        vo.setRefundAmount(record.getRefundAmount());
        vo.setRefundReason(record.getRefundReason());
        vo.setOperatorName(record.getOperatorName());
        vo.setRefundTime(record.getRefundTime());
        return vo;
    }

    private static String payMethodName(String code) {
        for (PayMethod payMethod : PayMethod.values()) {
            if (payMethod.getCode().equals(code)) {
                return payMethod.getDescription();
            }
        }
        return code;
    }
}
