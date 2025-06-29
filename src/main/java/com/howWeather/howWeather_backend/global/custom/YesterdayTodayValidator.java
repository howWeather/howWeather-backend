package com.howWeather.howWeather_backend.global.custom;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class YesterdayTodayValidator implements ConstraintValidator<YesterdayOrToday, LocalDate> {
    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) return true;
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate today = LocalDate.now();
        return !value.isBefore(yesterday) && !value.isAfter(today);
    }
}
