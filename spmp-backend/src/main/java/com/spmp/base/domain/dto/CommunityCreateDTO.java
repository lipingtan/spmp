package com.spmp.base.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 小区新增请求 DTO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class CommunityCreateDTO {

    /** 小区名称 */
    @NotBlank(message = "小区名称不能为空")
    @Size(max = 128, message = "小区名称不能超过128个字符")
    private String communityName;

    /** 小区编码（可选，为空时自动生成） */
    private String communityCode;

    /** 小区地址 */
    @NotBlank(message = "小区地址不能为空")
    @Size(max = 256, message = "小区地址不能超过256个字符")
    private String address;

    /** 所属片区ID */
    @NotNull(message = "所属片区不能为空")
    private Long districtId;

    /** 联系电话 */
    @Size(max = 20, message = "联系电话不能超过20个字符")
    private String phone;

    /** 物业公司名称 */
    @Size(max = 128, message = "物业公司名称不能超过128个字符")
    private String propertyCompany;

    /** 小区面积（平方米） */
    private BigDecimal area;

    /** 经度 */
    private BigDecimal longitude;

    /** 纬度 */
    private BigDecimal latitude;

    /** 小区图片URL */
    @Size(max = 512, message = "图片URL不能超过512个字符")
    private String image;

    /** 备注 */
    @Size(max = 256, message = "备注不能超过256个字符")
    private String remark;
}
