package com.howWeather.howWeather_backend.domain.ai_model.dto;

import lombok.Data;

import java.util.List;

@Data
public class ClothingRecommendationDto {
    private Long userId;
    private List<RecommendationResult> result;
}
