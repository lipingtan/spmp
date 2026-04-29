package com.spmp.notice.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 公告模块错误码（6000-6999）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum NoticeErrorCode {

    NOTICE_NOT_FOUND(6001, "公告不存在"),
    NOTICE_STATUS_INVALID(6002, "公告状态不允许当前操作"),
    NOTICE_PERMISSION_DENIED(6003, "无权操作该公告"),
    NOTICE_SCOPE_INVALID(6004, "公告推送范围无效"),
    NOTICE_RICH_TEXT_INVALID(6005, "公告内容不符合安全规范"),
    NOTICE_APPROVAL_NOT_FOUND(6006, "审批实例不存在"),
    NOTICE_APPROVAL_STATUS_INVALID(6007, "审批状态不允许当前操作"),
    NOTICE_REPUSH_IDEMPOTENT_HIT(6008, "重复重推请求已忽略"),
    NOTICE_PARAM_INVALID(6009, "请求参数不合法");

    private final int code;
    private final String message;
}
