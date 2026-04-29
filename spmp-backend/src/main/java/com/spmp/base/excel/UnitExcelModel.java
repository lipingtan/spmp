package com.spmp.base.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 单元 Excel 导入模型。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class UnitExcelModel {

    @ExcelProperty("单元名称")
    private String unitName;

    @ExcelProperty("单元编号")
    private String unitCode;

    @ExcelProperty("所属小区名称")
    private String communityName;

    @ExcelProperty("所属楼栋编号")
    private String buildingCode;

    @ExcelProperty("备注")
    private String remark;
}
