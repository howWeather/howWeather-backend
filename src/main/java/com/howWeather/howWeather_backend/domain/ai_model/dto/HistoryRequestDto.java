package com.howWeather.howWeather_backend.domain.ai_model.dto;

import com.howWeather.howWeather_backend.global.custom.ValidRange;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HistoryRequestDto {
    public static final int DEFAULT_CNT = 10;
    public static final double DEFAULT_GAP = 2.0;

    @NotNull
    private Long memberId;

    @NotNull @Max(60) @Min(-50)
    private double temperature;

    @ValidRange(min = 0, max = 20)
    private Integer cnt;

    @ValidRange(min = 0, max = 10)
    private Double upperGap;

    @ValidRange(min = 0, max = 10)
    private Double lowerGap;

    public double getUpperBound() {
        return temperature + (upperGap != null ? upperGap : DEFAULT_GAP);
    }

    public double getLowerBound() {
        return temperature - (lowerGap != null ? lowerGap : DEFAULT_GAP);
    }
}
