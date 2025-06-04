package com.howWeather.howWeather_backend.domain.ai_model.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ClothingCombinationDto {
    private List<Integer> top;
    private List<Integer> outer;
}
