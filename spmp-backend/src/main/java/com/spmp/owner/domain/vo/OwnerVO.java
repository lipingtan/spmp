package com.spmp.owner.domain.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 业主详情 VO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class OwnerVO {

    /** 业主ID */
    private Long id;

    /** 关联用户ID */
    private Long userId;

    /** 业主姓名 */
    private String ownerName;

    /** 业主姓名（前端编辑用，与 ownerName 相同） */
    private String name;

    /** 手机号（脱敏） */
    private String phoneMasked;

    /** 手机号（明文，仅编辑场景返回） */
    private String phone;

    /** 身份证号（脱敏） */
    private String idCardMasked;

    /** 身份证号（明文，仅编辑场景返回） */
    private String idCard;

    /** 性别 */
    private Integer gender;

    /** 头像URL */
    private String avatar;

    /** 邮箱 */
    private String email;

    /** 紧急联系人 */
    private String emergencyContact;

    /** 紧急联系电话（脱敏） */
    private String emergencyPhoneMasked;

    /** 紧急联系电话（明文，仅编辑场景返回） */
    private String emergencyPhone;

    /** 业主来源 */
    private String ownerSource;

    /** 业主状态 */
    private String ownerStatus;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 房产绑定列表 */
    private List<PropertyBindingVO> propertyBindings;

    /** 家庭成员列表 */
    private List<FamilyMemberVO> familyMembers;

    /** 认证历史 */
    private List<CertificationVO> certifications;
}
