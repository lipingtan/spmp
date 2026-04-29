package com.spmp.owner.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * H5 认证申请请求 DTO。
 * <p>
 * 前端传入 communityId/buildingId/unitId/houseId，后端只需 houseId，
 * 其余字段通过 @JsonIgnoreProperties 忽略，避免反序列化报错。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class H5CertificationCreateDTO {

    /** 房屋ID */
    @NotNull(message = "房屋ID不能为空")
    private Long houseId;
}
