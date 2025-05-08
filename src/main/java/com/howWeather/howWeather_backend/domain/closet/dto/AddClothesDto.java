package com.howWeather.howWeather_backend.domain.closet.dto;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class AddClothesDto {
    @Valid
    private List<ClothRegisterDto> uppers;

    @Valid
    private List<ClothRegisterDto> outers;
}
