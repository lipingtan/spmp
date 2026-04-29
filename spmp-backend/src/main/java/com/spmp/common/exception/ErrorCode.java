package com.spmp.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一错误码枚举。
 * <p>
 * 各业务模块预留码段：
 * <ul>
 *   <li>通用: 200, 400-500</li>
 *   <li>业务通用: 1000-1999</li>
 *   <li>user: 2000-2999</li>
 *   <li>owner: 3000-3999</li>
 *   <li>workorder: 4000-4999</li>
 *   <li>billing: 5000-5999</li>
 *   <li>notice: 6000-6999</li>
 *   <li>access: 7000-7999</li>
 *   <li>base: 8000-8999</li>
 * </ul>
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 通用 HTTP 错误码
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "不支持的请求方法"),
    INTERNAL_ERROR(500, "系统内部错误"),

    // 业务通用错误码 1000-1999
    PARAM_INVALID(1001, "参数校验失败"),
    DATA_NOT_FOUND(1002, "数据不存在"),
    DATA_ALREADY_EXISTS(1003, "数据已存在"),
    OPERATION_FAILED(1004, "操作失败");

    // 各模块预留码段（由各模块自行扩展）
    // user:      2000-2999
    // owner:     3000-3999
    // workorder: 4000-4999
    // billing:   5000-5999
    // notice:    6000-6999
    // access:    7000-7999
    // base:      8000-8999

    private final int code;
    private final String message;
}
