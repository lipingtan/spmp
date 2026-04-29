package com.spmp.owner.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业主管理模块错误码枚举（4000-4599）。
 * <p>
 * 错误码段分配：
 * <ul>
 *   <li>4000-4099: 业主信息管理</li>
 *   <li>4100-4199: 房产绑定管理</li>
 *   <li>4200-4299: 家庭成员管理</li>
 *   <li>4300-4399: 业主认证</li>
 *   <li>4400-4499: OwnerApi</li>
 *   <li>4500-4599: 加密/脱敏</li>
 * </ul>
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum OwnerErrorCode {

    // ========== 业主信息管理 4000-4099 ==========
    OWNER_NOT_FOUND(4000, "业主不存在"),
    OWNER_PHONE_DUPLICATE(4001, "手机号已存在"),
    OWNER_ID_CARD_DUPLICATE(4002, "身份证号已存在"),
    OWNER_HAS_PENDING_WORKORDER(4003, "业主存在未完结工单，无法删除"),
    OWNER_HAS_UNPAID_BILL(4004, "业主存在未缴清账单，无法删除"),
    OWNER_ALREADY_DISABLED(4005, "业主已处于停用状态"),
    OWNER_NOT_DISABLED(4006, "业主未处于停用状态，无法启用"),
    OWNER_PHONE_FORMAT_INVALID(4007, "手机号格式不正确"),
    OWNER_ID_CARD_FORMAT_INVALID(4008, "身份证号格式不正确"),

    // ========== 房产绑定管理 4100-4199 ==========
    BINDING_NOT_FOUND(4100, "绑定记录不存在"),
    BINDING_ALREADY_UNBOUND(4101, "绑定关系已解除"),
    BINDING_HOUSE_NOT_FOUND(4102, "房屋不存在"),
    BINDING_RELATION_TYPE_INVALID(4103, "关系类型无效"),

    // ========== 家庭成员管理 4200-4299 ==========
    FAMILY_MEMBER_NOT_FOUND(4200, "家庭成员不存在"),
    FAMILY_MEMBER_LIMIT_EXCEEDED(4201, "房产绑定人数已达上限（10人）"),
    FAMILY_MEMBER_NOT_BELONG(4202, "该家庭成员不属于当前业主"),
    FAMILY_MEMBER_RELATION_INVALID(4203, "家庭关系类型无效"),

    // ========== 业主认证 4300-4399 ==========
    CERTIFICATION_NOT_FOUND(4300, "认证申请不存在"),
    CERTIFICATION_ALREADY_PROCESSED(4301, "认证申请已处理"),
    CERTIFICATION_REJECT_REASON_REQUIRED(4302, "驳回原因不能为空"),
    CERTIFICATION_BATCH_LIMIT_EXCEEDED(4303, "批量审批数量超过上限（100条）"),
    CERTIFICATION_DUPLICATE_PENDING(4304, "该房屋已有待审批的认证申请"),
    OWNER_NOT_CERTIFIED(4305, "业主未完成认证，无法使用该功能"),
    CAPTCHA_INVALID(4306, "验证码错误"),

    // ========== OwnerApi 4400-4499 ==========
    OWNER_API_QUERY_FAILED(4400, "业主信息查询失败"),
    OWNER_API_CACHE_WRITE_FAILED(4401, "缓存写入失败"),

    // ========== 加密/脱敏 4500-4599 ==========
    ENCRYPT_FAILED(4500, "数据加密失败"),
    DECRYPT_FAILED(4501, "数据解密失败");

    private final int code;
    private final String message;
}
