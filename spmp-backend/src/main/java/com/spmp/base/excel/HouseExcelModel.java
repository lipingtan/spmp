package com.spmp.base.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 房屋 Excel 导入模型。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class HouseExcelModel {

    @ExcelProperty("房屋编号")
    private String houseCode;

    @ExcelProperty("所属小区名称")
    private String communityName;

    @ExcelProperty("所属楼栋编号")
    private String buildingCode;

    @ExcelProperty("所属单元编号")
    private String unitCode;

    @ExcelProperty("楼层")
    private Integer floor;

    @ExcelProperty("建筑面积(㎡)")
    private BigDecimal buildingArea;

    @ExcelProperty("使用面积(㎡)")
    private BigDecimal usableArea;

    @ExcelProperty("房屋状态")
    private String houseStatus;

    @ExcelProperty("房屋类型")
    private String houseType;

    @ExcelProperty("备注")
    private String remark;
}
