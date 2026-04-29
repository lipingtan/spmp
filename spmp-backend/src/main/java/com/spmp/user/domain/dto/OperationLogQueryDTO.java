package com.spmp.user.domain.dto;

import lombok.Data;
import java.util.Date;

/**
 * 操作日志查询条件。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class OperationLogQueryDTO {

    private String operatorName;
    private String module;
    private String operationType;
    private Date startTime;
    private Date endTime;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
