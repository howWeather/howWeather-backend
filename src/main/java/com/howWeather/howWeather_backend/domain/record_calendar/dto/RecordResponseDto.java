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
    int feeling;
    LocalDate date;
    List<Integer> uppers;
    List<Integer> outers;
}
