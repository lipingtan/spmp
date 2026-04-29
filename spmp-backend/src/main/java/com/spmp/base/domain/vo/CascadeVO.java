package com.spmp.base.domain.vo;

import lombok.Data;

/**
 * 级联选择器节点 VO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class CascadeVO {

    /** 节点ID */
    private Long id;

    /** 节点名称 */
    private String name;

    /** 节点编码 */
    private String code;

    /** 是否有下级数据 */
    private Boolean hasChildren;
}
