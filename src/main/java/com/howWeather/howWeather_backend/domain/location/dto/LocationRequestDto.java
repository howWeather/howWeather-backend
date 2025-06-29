package com.howWeather.howWeather_backend.domain.location.dto;

import lombok.Data;

@Data
public class LocationRequestDto {
    private double latitude;
    private double longitude;
}
