package com.spmp.notice.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告创建请求 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class NoticeCreateDTO {

    @NotBlank(message = "公告标题不能为空")
    @Size(max = 200, message = "公告标题长度不能超过200")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    private String content;

    @NotBlank(message = "公告类型不能为空")
    private String noticeType;

    private Integer topFlag = 0;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expireTime;

    @NotEmpty(message = "推送范围不能为空")
    private List<NoticeScopeDTO> scopeList;
}
