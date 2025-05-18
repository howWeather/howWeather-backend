package com.howWeather.howWeather_backend.global.custom;

import com.howWeather.howWeather_backend.domain.record_calendar.dto.RecordRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UppersOrOutersValidator implements ConstraintValidator<ValidateUppersOrOuters, RecordRequestDto> {

    @Override
    public boolean isValid(RecordRequestDto dto, ConstraintValidatorContext context) {
        if (dto == null) return true;

        boolean uppersEmpty = dto.getUppers() == null || dto.getUppers().isEmpty();
        boolean outersEmpty = dto.getOuters() == null || dto.getOuters().isEmpty();

        return !(uppersEmpty && outersEmpty);
    }
}
