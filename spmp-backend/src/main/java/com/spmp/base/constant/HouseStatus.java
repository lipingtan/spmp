package com.spmp.base.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 房屋状态枚举。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum HouseStatus {

    /** 空置 */
    VACANT("VACANT", "空置"),
    /** 已入住 */
    OCCUPIED("OCCUPIED", "已入住"),
    /** 已出租 */
    RENTED("RENTED", "已出租"),
    /** 装修中 */
    RENOVATING("RENOVATING", "装修中");

    private final String code;
    private final String description;

    /**
     * 根据编码获取枚举值。
     *
     * @param code 编码
     * @return 枚举值，不存在返回 null
     */
    public static HouseStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (HouseStatus status : values()) {
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
