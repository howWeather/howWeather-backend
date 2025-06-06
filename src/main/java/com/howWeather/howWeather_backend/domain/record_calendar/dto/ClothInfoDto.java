package com.howWeather.howWeather_backend.domain.record_calendar.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClothInfoDto {
    private Long clothType;
    private int color;
    private Long id;
}
