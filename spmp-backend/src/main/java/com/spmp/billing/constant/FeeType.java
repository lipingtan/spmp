package com.spmp.billing.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 费用类型枚举。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum FeeType {

    PROPERTY_FEE("PROPERTY_FEE", "物业费"),
    PARKING_FEE("PARKING_FEE", "停车费"),
    WATER_FEE("WATER_FEE", "水费"),
    ELECTRICITY_FEE("ELECTRICITY_FEE", "电费"),
    GAS_FEE("GAS_FEE", "燃气费");

    private final String code;
    private final String description;

    public static FeeType fromCode(String code) {
        for (FeeType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
