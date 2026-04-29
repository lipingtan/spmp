package com.spmp.workorder.domain.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 维修完成 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class WorkOrderCompleteDTO {

    @NotBlank(message = "处理说明不能为空")
    @Size(max = 1000, message = "处理说明不能超过1000个字符")
    private String repairDescription;

    @NotNull(message = "维修时长不能为空")
    @Min(value = 1, message = "维修时长至少1分钟")
    private Integer repairDuration;

    private List<String> imageUrls;

    private List<RepairMaterialDTO> materials;
}
