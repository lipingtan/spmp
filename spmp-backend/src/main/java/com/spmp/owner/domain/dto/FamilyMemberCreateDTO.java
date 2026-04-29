package com.spmp.owner.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 家庭成员添加请求 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class FamilyMemberCreateDTO {

    /** 成员姓名 */
    @NotBlank(message = "姓名不能为空")
    @Size(max = 64, message = "姓名不能超过64个字符")
    private String memberName;

    /** 手机号 */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 身份证号 */
    @NotBlank(message = "身份证号不能为空")
    @Size(min = 18, max = 18, message = "身份证号必须为18位")
    private String idCard;

    /** 与业主关系（SPOUSE-配偶 / PARENT-父母 / CHILD-子女 / OTHER-其他） */
    @NotBlank(message = "与业主关系不能为空")
    private String relation;
}
