package com.spmp.workorder.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 业主提交报修 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class WorkOrderCreateDTO {

    @NotBlank(message = "报修类型不能为空")
    private String orderType;

    @NotBlank(message = "问题描述不能为空")
    @Size(max = 1000, message = "问题描述不能超过1000个字符")
    private String description;

    @NotBlank(message = "地址类型不能为空")
    private String addressType;

    @NotNull(message = "小区不能为空")
    private Long communityId;

    private Long houseId;

    private Long buildingId;

    private Long unitId;

    private List<String> imageUrls;
}
