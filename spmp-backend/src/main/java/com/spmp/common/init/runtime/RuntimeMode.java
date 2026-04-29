package com.spmp.common.init.runtime;

/**
 * 初始化运行态枚举。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public enum RuntimeMode {

    /** 初始化模式，仅允许初始化接口访问。 */
    INIT,

    /** 业务模式，允许正常业务访问。 */
    BUSINESS
}
