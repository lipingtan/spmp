package com.spmp.workorder.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 图片类型枚举。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ImageType {

    REPORT("REPORT", "报修图片"),
    REPAIR("REPAIR", "维修图片"),
    REJECT("REJECT", "验收不通过图片");

    private final String code;
    private final String description;
}
