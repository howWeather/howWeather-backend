package com.howWeather.howWeather_backend.domain.ai_model.dto;

import lombok.Data;

import java.util.List;

@Data
public class RecommendPredictDto {
    private List<Integer> uppersTypeList;
    private List<Integer> outersTypeList;
    private List<WeatherFeelingDto> feelingList;
}
