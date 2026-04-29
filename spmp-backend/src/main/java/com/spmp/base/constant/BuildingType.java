package com.spmp.base.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 楼栋类型枚举。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum BuildingType {

    /** 住宅 */
    RESIDENTIAL("RESIDENTIAL", "住宅"),
    /** 商业 */
    COMMERCIAL("COMMERCIAL", "商业"),
    /** 车库 */
    GARAGE("GARAGE", "车库"),
    /** 混合 */
    MIXED("MIXED", "混合"),
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
    public static BuildingType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (BuildingType type : values()) {
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
