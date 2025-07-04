package com.howWeather.howWeather_backend.global.custom;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = YesterdayTodayValidator.class)
@Documented
public @interface YesterdayOrToday {
    String message() default "날짜는 어제 또는 오늘이어야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}