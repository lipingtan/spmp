package com.spmp.workorder.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 派发类型枚举。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum DispatchType {

    MANUAL("MANUAL", "手动派发"),
    AUTO("AUTO", "自动派发");

    private final String code;
    private final String description;
}
