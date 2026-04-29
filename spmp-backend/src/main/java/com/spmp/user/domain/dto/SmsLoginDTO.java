package com.spmp.user.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 手机号验证码登录入参。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class SmsLoginDTO {

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    private String smsCode;

    @NotBlank(message = "客户端类型不能为空")
    private String clientType;
}
