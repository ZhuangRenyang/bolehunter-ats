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
