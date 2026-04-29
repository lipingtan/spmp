package com.spmp.notice.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 公告审批 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class NoticeApproveDTO {

    @NotBlank(message = "审批动作不能为空")
    private String action;

    @Size(max = 256, message = "审批备注不能超过256字符")
    private String remark;
}
