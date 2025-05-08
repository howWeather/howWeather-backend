package com.howWeather.howWeather_backend.domain.closet.dto;

import lombok.Data;

import java.util.List;

@Data
public class ClothListDto {
    private String category;
    private List<ClothDetailDto> clothList;
}
