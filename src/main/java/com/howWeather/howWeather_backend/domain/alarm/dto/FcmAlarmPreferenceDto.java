package com.howWeather.howWeather_backend.domain.alarm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FcmAlarmPreferenceDto {
    private Boolean morning;
    private Boolean afternoon;
    private Boolean evening;
}
