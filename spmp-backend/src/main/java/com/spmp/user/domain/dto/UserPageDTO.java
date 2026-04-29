package com.spmp.user.domain.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 用户分页列表出参。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class UserPageDTO {

    private Long id;
    private String username;
    private String realName;
    /** 手机号（脱敏） */
    private String phone;
    private Integer status;
    private List<String> roles;
    private Date createTime;
}
