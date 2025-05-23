package com.howWeather.howWeather_backend.domain.alarm.dto;

import lombok.Data;

@Data
public class FcmTokenRequestDto {
    private String userId;
    private String token;
}
