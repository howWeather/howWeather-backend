package com.howWeather.howWeather_backend.domain.ai_model.dto;

import lombok.Data;

@Data
public class WeatherPredictDto {
    private int hour;
    private double temperature;
    private int humidity;
    private double windSpeed;
    private double precipitation;
    private int cloudAmount;
    private double feelsLike;
}
