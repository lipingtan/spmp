package com.spmp.owner.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 家庭关系枚举（家庭成员与业主的关系）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum FamilyRelation {

    /** 配偶 */
    SPOUSE("SPOUSE", "配偶"),
    /** 父母 */
    PARENT("PARENT", "父母"),
    /** 子女 */
    CHILD("CHILD", "子女"),
    /** 其他 */
    OTHER("OTHER", "其他");

    private final String code;
    private final String description;

    /**
     * 根据编码获取枚举值。
     *
     * @param code 编码
     * @return 枚举值，不存在返回 null
     */
    public static FamilyRelation fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (FamilyRelation relation : values()) {
            if (relation.code.equals(code)) {
                return relation;
            }
        }
        return null;
    }

    /**
     * 判断编码是否有效。
     *
     * @param code 编码
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }
}
