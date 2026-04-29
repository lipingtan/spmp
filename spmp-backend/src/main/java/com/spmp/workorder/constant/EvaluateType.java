package com.spmp.workorder.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 评价类型枚举。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum EvaluateType {

    OWNER("OWNER", "业主评价"),
    AUTO("AUTO", "自动验收默认");

    private final String code;
    private final String description;
}
