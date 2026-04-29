package com.spmp.user.domain.dto;

import lombok.Data;
import java.util.Date;

/**
 * 登录日志查询条件。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class LoginLogQueryDTO {

    private String username;
    private String loginIp;
    private Integer loginResult;
    private Date startTime;
    private Date endTime;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
