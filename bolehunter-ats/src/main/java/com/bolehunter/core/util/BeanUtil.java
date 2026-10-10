/*
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ZhuangRenyang
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以依据自由软件基金会发布的 GNU AGPL 第 3 版（或任一更高版本）的条款重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 */
package com.bolehunter.core.util;

import cn.hutool.core.util.StrUtil;

import java.lang.reflect.Method;

/**
 * 反射取值工具（替代 talelin BeanUtil，供日志模板使用）
 */
public class BeanUtil {

    private BeanUtil() {
    }

    /**
     * 按 getter 约定取属性值，取不到返回空串
     */
    public static String getValueByPropName(Object object, String propName) {
        if (object == null || StrUtil.isBlank(propName)) {
            return "";
        }
        try {
            String getter = "get" + Character.toUpperCase(propName.charAt(0)) + propName.substring(1);
            Method method = object.getClass().getMethod(getter);
            Object value = method.invoke(object);
            return value == null ? "" : String.valueOf(value);
        } catch (Exception e) {
            return "";
        }
    }
}
