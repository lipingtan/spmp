package com.spmp.common.exception;

import lombok.Getter;

/**
 * 业务异常类。
 * <p>
 * 所有业务逻辑异常统一使用此类抛出，包含错误码和错误信息。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String message;
    private final int code;

    /**
     * 使用 ErrorCode 默认 message 构造业务异常。
     *
     * @param errorCode 错误码枚举
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
        this.code = errorCode.getCode();
    }

    /**
     * 使用自定义 message 构造业务异常。
     *
     * @param errorCode 错误码枚举
     * @param message   自定义错误信息
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
        this.code = errorCode.getCode();
    }

    /**
     * 使用错误码和消息构造业务异常（支持模块级错误码）。
     *
     * @param code    错误码
     * @param message 错误信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.errorCode = null;
        this.message = message;
        this.code = code;
    }

    /**
     * 获取错误码数值。
     *
     * @return 错误码
     */
    public int getCode() {
        return errorCode != null ? errorCode.getCode() : code;
    }
}
