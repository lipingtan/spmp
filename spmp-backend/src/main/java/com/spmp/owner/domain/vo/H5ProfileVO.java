package com.spmp.owner.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * H5 个人信息 VO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class H5ProfileVO {

    /** 业主ID */
    private Long id;

    /** 业主姓名 */
    private String ownerName;

    /** 手机号（脱敏） */
    private String phoneMasked;

    /** 身份证号（脱敏） */
    private String idCardMasked;

    /** 性别 */
    private Integer gender;

    /** 头像URL */
    private String avatar;

    /** 邮箱 */
    private String email;

    /** 业主状态 */
    private String ownerStatus;

    /** 房产绑定列表 */
    private List<PropertyBindingVO> properties;
}
