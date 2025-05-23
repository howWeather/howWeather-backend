package com.howWeather.howWeather_backend.domain.alarm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FcmAlarmPreferenceDto {
    private boolean morning;
    private boolean afternoon;
    private boolean evening;
}