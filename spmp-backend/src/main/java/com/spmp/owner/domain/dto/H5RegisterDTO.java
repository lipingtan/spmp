package com.spmp.owner.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * H5 业主注册请求 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class H5RegisterDTO {

    /** 手机号 */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度6-20位")
    private String password;

    /** 业主姓名 */
    @NotBlank(message = "姓名不能为空")
    private String ownerName;

    /** 身份证号（可选） */
    private String idCard;

    /** 验证码（演示用，固定 123456 通过） */
    @NotBlank(message = "验证码不能为空")
    private String captcha;
}
