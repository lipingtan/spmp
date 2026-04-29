package com.spmp.billing.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 缴费管理模块错误码枚举（6000-6999）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum BillingErrorCode {

    BILL_NOT_FOUND(6001, "账单不存在"),
    INVALID_STATUS_TRANSITION(6002, "账单状态流转不合法"),
    BILL_ALREADY_PAID(6003, "账单已缴费"),
    BILL_ALREADY_CANCELLED(6004, "账单已取消"),
    BILL_ALREADY_OVERDUE(6005, "账单已逾期"),
    FEE_CONFIG_NOT_FOUND(6006, "费用配置不存在"),
    FEE_CONFIG_DUPLICATE(6007, "费用配置重复"),
    FEE_CONFIG_IN_USE(6008, "费用配置使用中，不可删除"),
    PAYMENT_NOT_FOUND(6009, "支付单不存在"),
    INVALID_PAYMENT_STATUS(6010, "支付单状态异常"),
    PAYMENT_EXPIRED(6011, "支付单已过期"),
    PAYMENT_AMOUNT_MISMATCH(6012, "支付金额不匹配"),
    PAYMENT_NOT_YOURS(6025, "非本人支付单"),
    INVALID_PAYMENT_CALLBACK(6026, "支付回调签名无效"),
    REFUND_AMOUNT_EXCEED(6027, "退款金额超过可退额度"),
    REDUCE_NOT_PENDING(6013, "减免记录非待审批状态"),
    REDUCE_AMOUNT_EXCEED(6014, "减免金额超过应收金额"),
    REDUCE_ALREADY_APPROVED(6015, "减免已审批"),
    REDUCE_ALREADY_REVOKED(6016, "减免已撤销"),
    GENERATE_LIMIT_EXCEEDED(6017, "批量生成数量超限"),
    NO_HOUSE_TO_GENERATE(6018, "无有效房屋可生成"),
    BATCH_BILL_NOT_ALL_UNPAID(6019, "批次账单非全部待缴费"),
    DUPLICATE_BILL_NO(6020, "账单编号重复"),
    EXPORT_LIMIT_EXCEEDED(6021, "导出数量超限"),
    BILL_NOT_YOURS(6022, "非本人账单"),
    OWNER_NOT_CERTIFIED(6023, "业主未认证"),
    HOUSE_NO_OWNER(6024, "房屋无业主绑定");

    private final int code;
    private final String message;
}
