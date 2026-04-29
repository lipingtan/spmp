package com.spmp.workorder.domain.vo;

import lombok.Data;

/**
 * 趋势图数据 VO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class TrendDataVO {

    private String date;

    private Long value;
}
