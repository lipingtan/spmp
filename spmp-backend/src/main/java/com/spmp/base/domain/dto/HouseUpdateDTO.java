package com.spmp.base.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * 房屋编辑请求 DTO。
 * <p>
 * 注意：不包含 unitId（禁止修改所属单元）。
 * 注意：不包含 houseStatus（状态通过专用接口变更）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class HouseUpdateDTO {

    /** 房屋编号 */
    @NotBlank(message = "房屋编号不能为空")
    private String houseCode;

    /** 楼层 */
    private Integer floor;

    /** 建筑面积（平方米） */
    private BigDecimal buildingArea;

    /** 使用面积（平方米） */
    private BigDecimal usableArea;

    /** 房屋类型 */
    private String houseType;

    /** 备注 */
    private String remark;
}
