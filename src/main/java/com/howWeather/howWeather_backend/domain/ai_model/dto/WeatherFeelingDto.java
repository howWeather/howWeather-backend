package com.howWeather.howWeather_backend.domain.ai_model.dto;

import lombok.Data;

@Data
public class WeatherFeelingDto {
    private int time;
    private double temperature;
    private int feeling;
}
