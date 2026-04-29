package com.spmp.workorder.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 验收工单 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class WorkOrderVerifyDTO {

    @NotNull(message = "验收结果不能为空")
    private Boolean passed;

    private String rejectReason;

    private List<String> rejectImageUrls;

    private Integer score;

    private String evaluateContent;
}
