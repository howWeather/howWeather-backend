package com.howWeather.howWeather_backend.global.custom;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = InvalidPrefixValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface InvalidPrefix {
    String message() default "아이디는 'Google_', 'Kakao_' 등의 접두사로 시작할 수 없습니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String[] prefixes(); // 금지할 접두사 목록
}
