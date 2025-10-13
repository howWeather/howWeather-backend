package com.howWeather.howWeather_backend.domain.ai_model.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@Getter
public class WeatherPredictDto {
    private int hour;
    private double temperature;
    private double humidity;
    private double windSpeed;
    private double precipitation;
    private double cloudAmount;
    private double feelsLike;
}
