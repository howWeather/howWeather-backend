package com.howWeather.howWeather_backend.domain.ai_model.dto;

import lombok.Data;

import java.util.List;

@Data
public class ModelClothingRecommendationDto {
    private Long userId;
    private List<ModelRecommendationResult> result;
}
