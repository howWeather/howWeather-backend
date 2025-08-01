package com.howWeather.howWeather_backend.domain.record_calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class RecordResponseDto {
    int timeSlot;
    double temperature;
    int feeling;
    LocalDate date;
    List<ClothInfoDto> uppers;
    List<ClothInfoDto> outers;
}
