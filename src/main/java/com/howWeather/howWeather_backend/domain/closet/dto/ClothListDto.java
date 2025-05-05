package com.howWeather.howWeather_backend.domain.closet.dto;

import lombok.Data;

import java.util.List;

@Data
public class ClothListDto {
    private String clothName;
    private List<ClothDetailDto> clothList;

}
