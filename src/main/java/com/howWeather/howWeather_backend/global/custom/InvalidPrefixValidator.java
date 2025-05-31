package com.howWeather.howWeather_backend.global.custom;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

public class InvalidPrefixValidator implements ConstraintValidator<InvalidPrefix, String> {

    private List<String> forbiddenPrefixes;

    @Override
    public void initialize(InvalidPrefix constraintAnnotation) {
        forbiddenPrefixes = Arrays.asList(constraintAnnotation.prefixes());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return forbiddenPrefixes.stream().noneMatch(value::startsWith);
    }
}

