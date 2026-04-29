package com.spmp.base.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 房屋类型枚举。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum HouseType {

    /** 住宅 */
    RESIDENCE("RESIDENCE", "住宅"),
    /** 商铺 */
    SHOP("SHOP", "商铺"),
    /** 车位 */
    PARKING("PARKING", "车位"),
    /** 办公 */
    OFFICE("OFFICE", "办公"),
    /** 其他 */
    OTHER("OTHER", "其他");

    private final String code;
    private final String description;

    /**
     * 根据编码获取枚举值。
     *
     * @param code 编码
     * @return 枚举值，不存在返回 null
     */
    public static HouseType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (HouseType type : values()) {
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
