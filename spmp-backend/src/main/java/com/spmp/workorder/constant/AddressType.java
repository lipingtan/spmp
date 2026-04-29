package com.spmp.workorder.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 地址类型枚举。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum AddressType {

    HOUSE("HOUSE", "房屋"),
    PUBLIC("PUBLIC", "公共区域");

    private final String code;
    private final String description;
}
