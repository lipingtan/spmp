package com.spmp.billing.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 减免审批状态枚举。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ReduceStatus {

    PENDING("PENDING", "待审批"),
    APPROVED("APPROVED", "已审批"),
    REJECTED("REJECTED", "已驳回"),
    REVOKED("REVOKED", "已撤销");

    private final String code;
    private final String description;
}
