package com.spmp.common.exception;

import com.spmp.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 * <p>
 * 拦截所有 Controller 层抛出的异常，统一返回 {@link Result} 格式响应。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常。
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        if (e.getErrorCode() != null) {
            return Result.fail(e.getErrorCode(), e.getMessage());
        }
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（@RequestBody 参数校验）。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        log.warn("参数校验失败: {}", message);
        return Result.fail(ErrorCode.PARAM_INVALID, message);
    }

    /**
     * 处理绑定异常（表单参数校验）。
     */
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        log.warn("参数绑定失败: {}", message);
        return Result.fail(ErrorCode.PARAM_INVALID, message);
    }

    /**
     * 处理不支持的 HTTP 方法异常。
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("不支持的请求方法: {}", e.getMethod());
        return Result.fail(ErrorCode.METHOD_NOT_ALLOWED, "不支持的请求方法: " + e.getMethod());
    }

    /**
     * 处理数据库连接异常。
     */
    @ExceptionHandler({
            java.sql.SQLException.class,
            com.alibaba.druid.pool.DataSourceClosedException.class,
            java.net.ConnectException.class,
            java.net.SocketTimeoutException.class
    })
    public Result<?> handleDatabaseException(Exception e) {
        log.error("数据库连接异常: ", e);
        return Result.fail(ErrorCode.INTERNAL_ERROR, "数据库连接失败，请检查数据库服务是否正常");
    }

    /**
     * 处理 Redis 连接异常。
     */
    @ExceptionHandler({
            io.lettuce.core.RedisConnectionException.class,
            io.lettuce.core.RedisException.class
    })
    public Result<?> handleRedisException(Exception e) {
        log.error("Redis连接异常: ", e);
        return Result.fail(ErrorCode.INTERNAL_ERROR, "缓存服务连接失败，请稍后重试");
    }

    /**
     * 处理未预期的异常。
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.fail(ErrorCode.INTERNAL_ERROR, "系统繁忙，请稍后重试");
    }
}
