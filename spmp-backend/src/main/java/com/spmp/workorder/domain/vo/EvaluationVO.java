package com.spmp.workorder.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价 VO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class EvaluationVO {

    private Long id;

    private Integer score;

    private String content;

    private Long evaluatorId;

    private LocalDateTime evaluateTime;

    private String evaluateType;
}
