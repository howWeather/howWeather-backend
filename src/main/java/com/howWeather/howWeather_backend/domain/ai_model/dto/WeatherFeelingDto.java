package com.howWeather.howWeather_backend.domain.ai_model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class WeatherFeelingDto {
    private LocalDate date;
    private int time;
    private double temperature;
    private int feeling;
}
