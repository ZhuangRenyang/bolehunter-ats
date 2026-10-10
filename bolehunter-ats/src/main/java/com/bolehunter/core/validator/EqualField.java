package com.bolehunter.core.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 两字段相等校验（密码确认等），原 lin-cms 同名注解平移
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EqualFieldValidator.class)
public @interface EqualField {

    String srcField();

    String dstField();

    String message() default "两字段不一致";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
