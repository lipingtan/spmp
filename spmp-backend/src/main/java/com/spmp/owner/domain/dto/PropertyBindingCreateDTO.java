package com.spmp.owner.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 房产绑定请求 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class PropertyBindingCreateDTO {

    /** 业主ID */
    @NotNull(message = "业主ID不能为空")
    private Long ownerId;

    /** 房屋ID */
    @NotNull(message = "房屋ID不能为空")
    private Long houseId;

    /** 关系类型（OWNER-业主 / TENANT-租户 / FAMILY-家属） */
    @NotBlank(message = "关系类型不能为空")
    private String relationType;
}
