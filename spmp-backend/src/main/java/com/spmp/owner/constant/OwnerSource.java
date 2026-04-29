package com.spmp.owner.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业主来源枚举。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum OwnerSource {

    /** 管理端录入 */
    ADMIN("ADMIN", "管理端录入"),
    /** H5 自助注册 */
    H5("H5", "H5自助注册");

    private final String code;
    private final String description;

    /**
     * 根据编码获取枚举值。
     *
     * @param code 编码
     * @return 枚举值，不存在返回 null
     */
    public static OwnerSource fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (OwnerSource source : values()) {
            if (source.code.equals(code)) {
                return source;
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
