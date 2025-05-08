package com.howWeather.howWeather_backend.global.custom;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidRangeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRange {
    String message() default "값은 {min} 이상 {max} 이하여야 합니다.";
    int min();
    int max();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
