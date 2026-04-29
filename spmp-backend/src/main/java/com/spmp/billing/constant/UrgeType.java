package com.spmp.billing.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 催收方式枚举。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum UrgeType {

    MESSAGE("MESSAGE", "站内消息"),
    SMS("SMS", "短信"),
    PHONE("PHONE", "电话");

    private final String code;
    private final String description;
}
