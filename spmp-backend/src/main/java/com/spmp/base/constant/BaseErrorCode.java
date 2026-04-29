package com.spmp.base.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 基础数据模块错误码枚举（3000-3999）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum BaseErrorCode {

    // ========== 片区管理 3000-3099 ==========
    DISTRICT_NAME_EXISTS(3001, "片区名称已存在"),
    DISTRICT_NOT_FOUND(3002, "片区不存在"),
    DISTRICT_HAS_COMMUNITIES(3003, "该片区下存在小区，无法删除"),
    DISTRICT_HAS_ACTIVE_COMMUNITIES(3004, "该片区下存在启用的小区，请先停用相关小区"),
    DISTRICT_DISABLED(3005, "该片区已停用，无法在其下新增小区"),

    // ========== 小区管理 3100-3199 ==========
    COMMUNITY_NAME_EXISTS(3101, "该片区下小区名称已存在"),
    COMMUNITY_NOT_FOUND(3102, "小区不存在"),
    COMMUNITY_HAS_BUILDINGS(3103, "该小区下存在楼栋，无法删除"),
    COMMUNITY_HAS_ACTIVE_BUILDINGS(3104, "该小区下存在启用的楼栋，请先停用相关楼栋"),
    COMMUNITY_DISABLED(3105, "该小区已停用，无法在其下新增楼栋"),
    COMMUNITY_CODE_EXISTS(3106, "小区编码已存在"),

    // ========== 楼栋管理 3200-3299 ==========
    BUILDING_CODE_EXISTS(3201, "该小区下楼栋编号已存在"),
    BUILDING_NOT_FOUND(3202, "楼栋不存在"),
    BUILDING_HAS_UNITS(3203, "该楼栋下存在单元，无法删除"),
    BUILDING_HAS_ACTIVE_UNITS(3204, "该楼栋下存在启用的单元，请先停用相关单元"),
    BUILDING_DISABLED(3205, "该楼栋已停用，无法在其下新增单元"),
    BUILDING_TYPE_INVALID(3206, "楼栋类型无效"),

    // ========== 单元管理 3300-3399 ==========
    UNIT_CODE_EXISTS(3301, "该楼栋下单元编号已存在"),
    UNIT_NOT_FOUND(3302, "单元不存在"),
    UNIT_HAS_HOUSES(3303, "该单元下存在房屋，只能停用不能删除"),
    UNIT_DISABLED(3305, "该单元已停用，无法在其下新增房屋"),

    // ========== 房屋管理 3400-3499 ==========
    HOUSE_CODE_EXISTS(3401, "该单元下房屋编号已存在"),
    HOUSE_NOT_FOUND(3402, "房屋不存在"),
    HOUSE_STATUS_INVALID(3403, "房屋状态无效"),
    HOUSE_TYPE_INVALID(3404, "房屋类型无效"),

    // ========== Excel 导入 3500-3599 ==========
    IMPORT_FILE_EMPTY(3501, "导入文件为空"),
    IMPORT_FILE_FORMAT_ERROR(3502, "文件格式不正确，请上传xlsx或xls文件"),
    IMPORT_EXCEED_LIMIT(3503, "单次导入不能超过5000条"),
    IMPORT_DATA_EMPTY(3504, "导入数据为空"),

    // ========== 编码生成 3700-3799 ==========
    CODE_GENERATE_FAILED(3701, "编码生成失败，请重试");

    private final int code;
    private final String message;
}
