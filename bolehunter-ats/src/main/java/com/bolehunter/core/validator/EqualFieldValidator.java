/*
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ZhuangRenyang
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以在 AGPL-3.0 许可下重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 */
package com.bolehunter.core.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Method;

/**
 * 两字段相等校验实现
 */
public class EqualFieldValidator implements ConstraintValidator<EqualField, Object> {

    private String srcField;

    private String dstField;

    @Override
    public void initialize(EqualField annotation) {
        this.srcField = annotation.srcField();
        this.dstField = annotation.dstField();
    }

    @Override
    public boolean isValid(Object bean, ConstraintValidatorContext context) {
        if (bean == null) {
            return true;
        }
        try {
            Object src = readField(bean, srcField);
            Object dst = readField(bean, dstField);
            if (src == null && dst == null) {
                return true;
            }
            return src != null && src.equals(dst);
        } catch (Exception e) {
            return false;
        }
    }

    private Object readField(Object bean, String fieldName) throws Exception {
        String getter = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        Method method = bean.getClass().getMethod(getter);
        return method.invoke(bean);
    }
}
