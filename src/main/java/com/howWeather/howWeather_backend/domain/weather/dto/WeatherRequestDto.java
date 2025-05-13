package com.howWeather.howWeather_backend.domain.weather.dto;

import lombok.Data;

@Data
public class WeatherRequestDto {
    private double lat;
    private double lon;
    private int timeSlot;
}
