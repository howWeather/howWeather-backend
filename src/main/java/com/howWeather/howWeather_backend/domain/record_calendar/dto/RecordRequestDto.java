package com.howWeather.howWeather_backend.domain.record_calendar.dto;

import com.howWeather.howWeather_backend.global.custom.ValidateUppersOrOuters;
import com.howWeather.howWeather_backend.global.custom.YesterdayOrToday;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@ValidateUppersOrOuters
public class RecordRequestDto {
    @Min(1) @Max(3)
    int timeSlot;

    @Min(1) @Max(3)
    int feeling;

    @YesterdayOrToday
    LocalDate date;

    List<Long> uppers;
    List<Long> outers;

    @NotBlank
    String city;
}
