package com.spmp.base.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Excel 导入错误信息。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcelErrorInfo {

    @ExcelProperty("行号")
    private Integer rowNum;

    @ExcelProperty("字段名")
    private String fieldName;

    @ExcelProperty("错误原因")
    private String errorMessage;
}
