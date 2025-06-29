package com.howWeather.howWeather_backend.domain.location.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegionTemperatureDto {
    private RegionInfoDto regionInfo;
    private Double temperature;
}
