package com.spmp.owner.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 关系类型枚举（业主与房屋的关系）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum RelationType {

    /** 业主 */
    OWNER("OWNER", "业主"),
    /** 租户 */
    TENANT("TENANT", "租户"),
    /** 家属 */
    FAMILY("FAMILY", "家属");

    private final String code;
    private final String description;

    /**
     * 根据编码获取枚举值。
     *
     * @param code 编码
     * @return 枚举值，不存在返回 null
     */
    public static RelationType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (RelationType type : values()) {
            if (type.code.equals(code)) {
                return type;
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
