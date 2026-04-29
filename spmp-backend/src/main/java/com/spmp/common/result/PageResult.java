package com.spmp.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;
import org.slf4j.MDC;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * 分页查询统一响应包装类。
 * <p>
 * 在 Result 基础上增加分页元数据（总条数、当前页码、每页条数）。
 *
 * @param <T> 分页数据元素类型
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 响应码 */
    private Integer code;

    /** 响应信息 */
    private String message;

    /** 分页数据列表 */
    private List<T> data;

    /** 请求追踪标识 */
    private String traceId;

    /** 总条数 */
    private Long total;

    /** 当前页码 */
    private Integer pageNum;

    /** 每页条数 */
    private Integer pageSize;

    /**
     * 从 MyBatis Plus IPage 对象构建分页响应。
     *
     * @param page MyBatis Plus 分页对象
     * @param <T>  数据元素类型
     * @return 分页响应对象
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(page.getRecords());
        result.setTotal(page.getTotal());
        result.setPageNum((int) page.getCurrent());
        result.setPageSize((int) page.getSize());
        result.setTraceId(getTraceId());
        return result;
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
