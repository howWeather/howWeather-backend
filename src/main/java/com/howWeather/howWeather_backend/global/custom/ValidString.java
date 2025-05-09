package com.howWeather.howWeather_backend.global.custom;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidStringValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidString {

    String message() default "문자열은 지정된 조건을 만족해야 합니다.";

    int min() default 0;
    int max() default Integer.MAX_VALUE;

    String regexp() default ".*";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
