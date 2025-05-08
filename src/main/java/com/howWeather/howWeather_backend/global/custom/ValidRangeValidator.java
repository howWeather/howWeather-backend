package com.howWeather.howWeather_backend.global.custom;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidRangeValidator implements ConstraintValidator<ValidRange, Integer> {

    private int min;
    private int max;

    @Override
    public void initialize(ValidRange constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return value >= min && value <= max;
    }
}
