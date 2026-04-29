package com.spmp.workorder.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 取消/关闭工单 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class WorkOrderCancelDTO {

    @NotBlank(message = "取消原因不能为空")
    @Size(max = 512, message = "取消原因不能超过512个字符")
    private String cancelReason;

    private String cancelType;
}
