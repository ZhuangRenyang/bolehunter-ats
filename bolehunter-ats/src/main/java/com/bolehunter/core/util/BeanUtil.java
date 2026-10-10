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
