package com.howWeather.howWeather_backend.domain.closet.dto;

import lombok.Data;

import java.util.List;

@Data
public class GroupedClothDto {
    private Long clothType;
    private List<ClothDetailDto> items;

    public GroupedClothDto(Long clothType, List<ClothDetailDto> items) {
        this.clothType = clothType;
        this.items = items;
    }
}
