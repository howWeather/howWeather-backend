package com.howWeather.howWeather_backend.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegionDto {
    @NotNull @NotBlank
    private String regionName;
}
