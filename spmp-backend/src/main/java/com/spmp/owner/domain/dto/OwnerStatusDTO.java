package com.spmp.owner.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 业主停用/启用请求 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class OwnerStatusDTO {

    /** 操作类型（DISABLE-停用 / ENABLE-启用） */
    @NotBlank(message = "操作类型不能为空")
    private String action;
}
