package com.spmp.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 操作日志表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@TableName("sys_operation_log")
public class OperationLogDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人 ID */
    private Long operatorId;

    /** 操作人用户名 */
    private String operatorName;

    /** 模块名称 */
    private String module;

    /** 操作类型 */
    private String operationType;

    /** 操作描述 */
    private String description;

    /** 请求方法 */
    private String requestMethod;

    /** 请求 URL */
    private String requestUrl;

    /** 请求参数（JSON） */
    private String requestParams;

    /** 响应结果（JSON） */
    private String responseResult;

    /** 变更前数据（JSON） */
    private String beforeData;

    /** 操作 IP */
    private String operationIp;

    /** 操作时间 */
    private Date operationTime;

    /** 耗时（ms） */
    private Long costTime;
}
