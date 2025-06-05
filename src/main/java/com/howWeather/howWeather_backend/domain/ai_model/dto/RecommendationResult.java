package com.howWeather.howWeather_backend.domain.ai_model.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RecommendationResult {
    private List<Integer> tops;
    private List<Integer> outers;
    private Map<String, Integer> predictFeeling;
}
