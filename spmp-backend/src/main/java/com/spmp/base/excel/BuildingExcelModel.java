package com.spmp.base.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 楼栋 Excel 导入模型。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class BuildingExcelModel {

    @ExcelProperty("楼栋名称")
    private String buildingName;

    @ExcelProperty("楼栋编号")
    private String buildingCode;

    @ExcelProperty("所属小区名称")
    private String communityName;

    @ExcelProperty("地上层数")
    private Integer aboveGroundFloors;

    @ExcelProperty("地下层数")
    private Integer undergroundFloors;

    @ExcelProperty("每层户数")
    private Integer unitsPerFloor;

    @ExcelProperty("楼栋类型")
    private String buildingType;

    @ExcelProperty("备注")
    private String remark;
}
