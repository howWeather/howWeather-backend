package com.howWeather.howWeather_backend.domain.alarm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FcmTokenRequestDto {
    @NotNull
    private String token;
}
