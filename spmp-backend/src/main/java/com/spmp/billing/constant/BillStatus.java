package com.spmp.billing.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账单状态枚举。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum BillStatus {

    UNPAID("UNPAID", "待缴费"),
    PAYING("PAYING", "待支付"),
    PAID("PAID", "已缴费"),
    OVERDUE("OVERDUE", "已逾期"),
    REDUCED("REDUCED", "已减免"),
    REFUNDED("REFUNDED", "已退款"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String description;

    public static BillStatus fromCode(String code) {
        for (BillStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
