package com.spmp.owner.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业主状态枚举。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum OwnerStatus {

    /** 未认证 */
    UNCERTIFIED("UNCERTIFIED", "未认证"),
    /** 认证中 */
    CERTIFYING("CERTIFYING", "认证中"),
    /** 已认证 */
    CERTIFIED("CERTIFIED", "已认证"),
    /** 已停用 */
    DISABLED("DISABLED", "已停用");

    private final String code;
    private final String description;

    /**
     * 根据编码获取枚举值。
     *
     * @param code 编码
     * @return 枚举值，不存在返回 null
     */
    public static OwnerStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (OwnerStatus status : values()) {
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
