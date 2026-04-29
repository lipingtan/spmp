package com.spmp.billing.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 计费方式枚举。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum BillingMethod {

    BY_AREA("BY_AREA", "按面积计费"),
    FIXED("FIXED", "固定月费"),
    BY_USAGE("BY_USAGE", "按用量计费");

    private final String code;
    private final String description;

    public static BillingMethod fromCode(String code) {
        for (BillingMethod method : values()) {
            if (method.getCode().equals(code)) {
                return method;
            }
        }
        return null;
    }
}
