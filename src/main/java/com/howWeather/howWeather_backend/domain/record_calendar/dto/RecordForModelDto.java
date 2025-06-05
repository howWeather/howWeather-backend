package com.howWeather.howWeather_backend.domain.record_calendar.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecordForModelDto {
    private List<Integer> tops;
    private List<Integer> outers;
    private double temperature;
    private int feeling;
}
