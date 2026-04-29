package com.spmp.notice.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 公告推送范围 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class NoticeScopeDTO {

    @NotBlank(message = "范围类型不能为空")
    private String scopeType;

    private Long targetId;
}
