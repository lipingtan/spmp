package com.spmp.dashboard.domain.vo;

import lombok.Data;

/**
 * Dashboard 通用模块返回结构。
 *
 * @param <T> 数据类型
 */
@Data
public class DashboardModuleVO<T> {

    private DashboardModuleStatus status;

    private String message;

    private T data;
}

