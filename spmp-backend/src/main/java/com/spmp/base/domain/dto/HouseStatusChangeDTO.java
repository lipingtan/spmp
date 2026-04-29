package com.spmp.base.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 房屋状态变更请求 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class HouseStatusChangeDTO {

    /** 房屋状态（VACANT/OCCUPIED/RENTED/RENOVATING） */
    @NotBlank(message = "房屋状态不能为空")
    private String houseStatus;
}
