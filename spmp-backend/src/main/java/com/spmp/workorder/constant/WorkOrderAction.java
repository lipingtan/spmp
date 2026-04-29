package com.spmp.workorder.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工单操作类型枚举。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum WorkOrderAction {

    CREATE("CREATE", "创建工单"),
    DISPATCH("DISPATCH", "手动派发"),
    AUTO_DISPATCH("AUTO_DISPATCH", "自动派发"),
    ACCEPT("ACCEPT", "接单"),
    COMPLETE("COMPLETE", "维修完成"),
    VERIFY_PASS("VERIFY_PASS", "验收通过"),
    VERIFY_REJECT("VERIFY_REJECT", "验收不通过"),
    TRANSFER("TRANSFER", "转派"),
    CANCEL("CANCEL", "取消"),
    FORCE_CLOSE("FORCE_CLOSE", "强制关闭"),
    TIMEOUT_RETURN("TIMEOUT_RETURN", "超时退回"),
    AUTO_VERIFY("AUTO_VERIFY", "自动验收"),
    URGE("URGE", "催单");

    private final String code;
    private final String description;
}
