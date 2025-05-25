package com.howWeather.howWeather_backend.domain.weather.dto;

import lombok.Data;

@Data
public class WeatherSimpleDto {
    private String city;
    private int timeSlot;
    private String date;
}