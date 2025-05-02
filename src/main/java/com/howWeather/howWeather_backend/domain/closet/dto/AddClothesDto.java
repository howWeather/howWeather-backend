package com.howWeather.howWeather_backend.domain.closet.dto;

import lombok.Data;

import java.util.List;

@Data
public class AddClothesDto {
    private List<ClothDto> uppers;
    private List<ClothDto> outers;
}
