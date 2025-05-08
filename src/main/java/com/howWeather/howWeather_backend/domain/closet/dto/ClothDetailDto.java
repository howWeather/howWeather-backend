package com.howWeather.howWeather_backend.domain.closet.dto;

import lombok.Data;

@Data
public class ClothDetailDto {
    private long clothType;
    private int color;
    private int thickness;
    private long clothId;
}
