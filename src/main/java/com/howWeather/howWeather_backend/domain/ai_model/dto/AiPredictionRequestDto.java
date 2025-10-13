package com.howWeather.howWeather_backend.domain.ai_model.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AiPredictionRequestDto {
    private Long userId;
    private int bodyTypeLabel;
    private List<WeatherPredictDto> weatherForecast;
    private List<ClothingCombinationDto> clothingCombinations;
}
