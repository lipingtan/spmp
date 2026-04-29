package com.spmp.owner.domain.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 业主编辑请求 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class OwnerUpdateDTO {

    /** 业主姓名 */
    @NotBlank(message = "姓名不能为空")
    @Size(max = 64, message = "姓名不能超过64个字符")
    private String ownerName;

    /** 手机号 */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 身份证号 */
    @NotBlank(message = "身份证号不能为空")
    @Size(min = 18, max = 18, message = "身份证号必须为18位")
    private String idCard;

    /** 性别（0-未知 1-男 2-女） */
    private Integer gender;

    /** 头像URL */
    private String avatar;

    /** 邮箱 */
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 紧急联系人 */
    private String emergencyContact;

    /** 紧急联系电话 */
    private String emergencyPhone;
}
