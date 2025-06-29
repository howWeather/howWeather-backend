package com.howWeather.howWeather_backend.domain.location.dto;

import lombok.Data;

@Data
public class RegionInfoDto {
    private String address_name;
    private String region_1depth_name;
    private String region_2depth_name;
}
