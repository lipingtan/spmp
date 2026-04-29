package com.spmp.user.domain.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 数据权限配置入参。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class DataPermissionConfigDTO {

    @NotBlank(message = "数据权限级别不能为空")
    private String dataPermissionLevel;

    /** 关联的数据 ID 列表 */
    private List<Long> dataIds;
}
