package com.spmp.notice.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class NoticeRepushDTO {

    @NotBlank(message = "幂等键不能为空")
    @Size(max = 64, message = "幂等键长度不能超过64")
    private String bizSerialNo;

    @Size(max = 256, message = "备注不能超过256字符")
    private String remark;
}
