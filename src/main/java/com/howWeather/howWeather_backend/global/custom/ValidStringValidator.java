package com.howWeather.howWeather_backend.global.custom;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidStringValidator implements ConstraintValidator<ValidString, String> {

    private int min;
    private int max;
    private String regexp;

    @Override
    public void initialize(ValidString constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
        this.regexp = constraintAnnotation.regexp();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true; 

        int length = value.length();
        if (length < min || length > max) {
            return false;
        }

        return value.matches(regexp);
    }
}
