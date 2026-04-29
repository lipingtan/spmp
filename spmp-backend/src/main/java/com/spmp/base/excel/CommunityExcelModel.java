package com.spmp.base.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 小区 Excel 导入模型。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class CommunityExcelModel {

    @ExcelProperty("小区名称")
    private String communityName;

    @ExcelProperty("小区编码")
    private String communityCode;

    @ExcelProperty("小区地址")
    private String address;

    @ExcelProperty("所属片区名称")
    private String districtName;

    @ExcelProperty("联系电话")
    private String phone;

    @ExcelProperty("物业公司")
    private String propertyCompany;

    @ExcelProperty("小区面积(㎡)")
    private BigDecimal area;
}
