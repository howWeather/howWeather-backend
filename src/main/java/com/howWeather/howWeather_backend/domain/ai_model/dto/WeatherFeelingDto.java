package com.howWeather.howWeather_backend.domain.ai_model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeatherFeelingDto {
    private int time;
    private double temperature;
    private int feeling;
}
