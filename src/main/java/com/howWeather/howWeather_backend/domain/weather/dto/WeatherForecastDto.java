package com.howWeather.howWeather_backend.domain.weather.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class WeatherForecastDto {
    private int hour;
    private double temperature;
    private double humidity;
    private double windSpeed;
    private double precipitation;
    private double cloudAmount;
    private double feelsLike;
}
