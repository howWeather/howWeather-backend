package com.howWeather.howWeather_backend.domain.weather.dto;

import lombok.Data;

@Data
public class WeatherResponseDto {
    private double temperature;
    private double humidity;
    private double windSpeed;
    private double precipitation;
    private double cloudiness;
    private double feelsLike;
    private String weatherDescription;
}
