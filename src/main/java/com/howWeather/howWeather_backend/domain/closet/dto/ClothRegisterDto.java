package com.howWeather.howWeather_backend.domain.closet.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClothRegisterDto {
    @NotBlank
    private String clothName;

    @NotNull @Min(1) @Max(11)
    private int color;

    @NotNull @Min(1) @Max(3)
    private int thickness;
}
