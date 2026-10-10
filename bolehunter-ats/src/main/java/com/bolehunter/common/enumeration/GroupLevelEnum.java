package com.bolehunter.common.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum GroupLevelEnum {
    /**
     * 超级管理员
     */
    ROOT(1),
    /**
     * 游客
     */
    GUEST(2),
    /**
     * 普通用户
     */
    USER(3);

    /**
     * MybatisEnumTypeHandler 按 @EnumValue 标注的字段做转换
     */
    @EnumValue
    private final Integer value;

    GroupLevelEnum(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return this.value;
    }
}
