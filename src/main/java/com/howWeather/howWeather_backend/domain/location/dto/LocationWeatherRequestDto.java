package com.howWeather.howWeather_backend.domain.location.dto;

import com.howWeather.howWeather_backend.global.custom.YesterdayOrToday;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LocationWeatherRequestDto {
    @DecimalMin(value = "-90.0", message = "latitude 최소값은 -90입니다.")
    @DecimalMax(value = "90.0", message = "latitude 최대값은 90입니다.")
    private double latitude;

    @DecimalMin(value = "-180.0", message = "longitude 최소값은 -180입니다.")
    @DecimalMax(value = "180.0", message = "longitude 최대값은 180입니다.")
    private double longitude;

    @Min(value = 1, message = "timeSlot은 1 이상이어야 합니다.")
    @Max(value = 3, message = "timeSlot은 3 이하여야 합니다.")
    private int timeSlot;

    @YesterdayOrToday
    private LocalDate date;
}
