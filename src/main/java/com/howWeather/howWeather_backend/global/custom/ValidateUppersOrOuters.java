package com.howWeather.howWeather_backend.global.custom;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UppersOrOutersValidator.class)
@Documented
public @interface ValidateUppersOrOuters {
    String message() default "상의와 아우터 리스트 중 적어도 하나는 비어있지 않아야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
