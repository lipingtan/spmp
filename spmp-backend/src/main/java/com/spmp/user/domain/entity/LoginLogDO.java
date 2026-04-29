package com.spmp.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 登录日志表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@TableName("sys_login_log")
public class LoginLogDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名 */
    private String username;

    /** 登录 IP */
    private String loginIp;

    /** 登录地点 */
    private String loginLocation;

    /** 浏览器 */
    private String browser;

    /** 操作系统 */
    private String os;

    /** 登录时间 */
    private Date loginTime;

    /** 登录结果（0-成功 1-失败） */
    private Integer loginResult;

    /** 失败原因 */
    private String failReason;
}
