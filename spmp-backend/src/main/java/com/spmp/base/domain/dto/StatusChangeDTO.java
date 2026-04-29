package com.spmp.base.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 状态变更请求 DTO（通用）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class StatusChangeDTO {

    /** 状态（0-启用 1-停用） */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
