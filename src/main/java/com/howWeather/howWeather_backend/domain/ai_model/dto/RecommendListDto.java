package com.howWeather.howWeather_backend.domain.ai_model.dto;

import lombok.Data;

import java.util.List;

@Data
public class RecommendListDto {
    private List<RecommendPredictDto> result;
}
