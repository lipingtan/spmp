package com.spmp.common.init.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 初始化状态响应体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitStatusVO {

    /** 系统是否已完成初始化 */
    private boolean initialized;
}
