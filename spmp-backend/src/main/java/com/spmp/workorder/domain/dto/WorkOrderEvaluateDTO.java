package com.spmp.workorder.domain.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 评价工单 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class WorkOrderEvaluateDTO {

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1星")
    @Max(value = 5, message = "评分最高5星")
    private Integer score;

    @Size(max = 512, message = "评价内容不能超过512个字符")
    private String content;
}
