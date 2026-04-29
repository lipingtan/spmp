package com.spmp.user.domain.vo;

import lombok.Data;
import java.util.List;

/**
 * 数据权限可选范围值对象。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class DataPermissionOptionVO {

    private List<OptionItem> areas;
    private List<OptionItem> communities;
    private List<OptionItem> buildings;

    @Data
    public static class OptionItem {
        private Long id;
        private String name;

        public OptionItem(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
