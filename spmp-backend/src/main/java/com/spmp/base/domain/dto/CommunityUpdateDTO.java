package com.spmp.base.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 小区编辑请求 DTO。
 * <p>
 * 注意：不包含 districtId（禁止修改所属片区）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class CommunityUpdateDTO {

    /** 小区名称 */
    @NotBlank(message = "小区名称不能为空")
    @Size(max = 128, message = "小区名称不能超过128个字符")
    private String communityName;

    /** 小区地址 */
    @NotBlank(message = "小区地址不能为空")
    private String address;

    /** 联系电话 */
    private String phone;

    /** 物业公司名称 */
    private String propertyCompany;

    /** 小区面积（平方米） */
    private BigDecimal area;

    /** 经度 */
    private BigDecimal longitude;

    /** 纬度 */
    private BigDecimal latitude;

    /** 小区图片URL */
    private String image;

    /** 备注 */
    private String remark;
}
