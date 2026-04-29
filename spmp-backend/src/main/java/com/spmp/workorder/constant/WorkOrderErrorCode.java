package com.spmp.workorder.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工单管理模块错误码枚举（5000-5999）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum WorkOrderErrorCode {

    WORK_ORDER_NOT_FOUND(5001, "工单不存在"),
    INVALID_STATUS_TRANSITION(5002, "工单状态流转不合法"),
    WORK_ORDER_ALREADY_DISPATCHED(5003, "工单已派发"),
    WORK_ORDER_ALREADY_ACCEPTED(5004, "工单已接单"),
    WORK_ORDER_ALREADY_CANCELLED(5005, "工单已取消"),
    WORK_ORDER_ALREADY_COMPLETED(5006, "工单已完成"),
    REPAIR_USER_NOT_FOUND(5007, "维修人员不存在"),
    NO_REPAIR_STAFF_AVAILABLE(5008, "无可用维修人员"),
    REJECT_LIMIT_EXCEEDED(5009, "验收不通过次数超限"),
    OWNER_NOT_CERTIFIED(5010, "业主未认证"),
    WORK_ORDER_NOT_YOURS(5011, "非本人工单"),
    IMAGE_LIMIT_EXCEEDED(5012, "图片数量超限"),
    EXPORT_LIMIT_EXCEEDED(5013, "导出数量超限（1000条）"),
    DUPLICATE_ORDER_NO(5014, "工单编号重复"),
    DISPATCH_RECORD_NOT_FOUND(5015, "派发记录不存在"),
    EVALUATION_ALREADY_EXISTS(5016, "评价已存在"),
    VERIFY_REJECT_REASON_REQUIRED(5017, "验收不通过原因必填"),
    VERIFY_REJECT_IMAGE_REQUIRED(5018, "验收不通过图片必填"),
    CANCEL_REASON_REQUIRED(5019, "取消原因必填"),
    WORK_ORDER_IN_PROGRESS(5020, "工单处理中，不可操作");

    private final int code;
    private final String message;
}
