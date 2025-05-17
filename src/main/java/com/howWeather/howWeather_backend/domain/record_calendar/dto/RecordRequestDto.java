package com.howWeather.howWeather_backend.domain.record_calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class RecordRequestDto {
    int timeSlot;
    int feeling;
    LocalDate date;
    List<Long> uppers;
    List<Long> outers;
}
