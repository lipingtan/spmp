package com.spmp.common.result;

import com.spmp.common.exception.ErrorCode;
import lombok.Data;
import org.slf4j.MDC;

import java.io.Serializable;
import java.util.UUID;

/**
 * 统一 API 响应包装类。
 * <p>
 * 所有接口统一返回此格式，包含 code、message、data、traceId 四个字段。
 *
 * @param <T> 响应数据类型
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 响应码 */
    private Integer code;

    /** 响应信息 */
    private String message;

    /** 响应数据 */
    private T data;

    /** 请求追踪标识 */
    private String traceId;

    /**
     * 构建成功响应。
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功的 Result 对象
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        result.setTraceId(getTraceId());
        return result;
    }

    /**
     * 构建失败响应（使用 ErrorCode 默认 message）。
     *
     * @param errorCode 错误码枚举
     * @param <T>       数据类型
     * @return 失败的 Result 对象
     */
    public static <T> Result<T> fail(ErrorCode errorCode) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getCode());
        result.setMessage(errorCode.getMessage());
        result.setTraceId(getTraceId());
        return result;
    }

    /**
     * 构建失败响应（自定义 message）。
     *
     * @param errorCode 错误码枚举
     * @param message   自定义错误信息
     * @param <T>       数据类型
     * @return 失败的 Result 对象
     */
    public static <T> Result<T> fail(ErrorCode errorCode, String message) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getCode());
        result.setMessage(message);
        result.setTraceId(getTraceId());
        return result;
    }

    /**
     * 构建失败响应（支持模块级错误码，不依赖 ErrorCode 枚举）。
     *
     * @param code    错误码
     * @param message 错误信息
     * @param <T>     数据类型
     * @return 失败的 Result 对象
     */
    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setTraceId(getTraceId());
        return result;
    }

    /**
     * 判断是否成功。
     *
     * @return code 等于 200 时返回 true
     */
    public boolean isSuccess() {
        return this.code != null && this.code == 200;
    }

    /**
     * 从 MDC 获取 traceId，若无则自动生成 UUID。
     */
    private static String getTraceId() {
        String traceId = MDC.get("traceId");
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        return traceId;
    }
}
