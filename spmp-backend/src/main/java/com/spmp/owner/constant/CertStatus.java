package com.spmp.owner.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 认证状态枚举。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum CertStatus {

    /** 待审批 */
    PENDING("PENDING", "待审批"),
    /** 已通过 */
    APPROVED("APPROVED", "已通过"),
    /** 已驳回 */
    REJECTED("REJECTED", "已驳回");

    private final String code;
    private final String description;

    /**
     * 根据编码获取枚举值。
     *
     * @param code 编码
     * @return 枚举值，不存在返回 null
     */
    public static CertStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (CertStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断编码是否有效。
     *
     * @param code 编码
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }
}
