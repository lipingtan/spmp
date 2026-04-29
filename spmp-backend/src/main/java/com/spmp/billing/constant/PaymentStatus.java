package com.spmp.billing.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付状态枚举。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum PaymentStatus {

    PENDING("PENDING", "待支付"),
    SUCCESS("SUCCESS", "支付成功"),
    FAILED("FAILED", "支付失败"),
    CLOSED("CLOSED", "已关闭");

    private final String code;
    private final String description;

    /**
     * 根据 code 安全查找枚举，未找到返回 null（避免 valueOf 抛异常）。
     */
    public static PaymentStatus fromCode(String code) {
        for (PaymentStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
