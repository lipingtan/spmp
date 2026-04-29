package com.spmp.workorder.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.spmp.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 评价记录表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wo_evaluation")
public class EvaluationDO extends BaseEntity {

    private Long orderId;

    private Integer score;

    private String content;

    private Long evaluatorId;

    private LocalDateTime evaluateTime;

    private String evaluateType;
}
