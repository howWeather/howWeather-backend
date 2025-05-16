package com.howWeather.howWeather_backend.domain.record_calendar.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int type;

    private int feeling;

    @OneToMany(mappedBy = "dayRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DayRecordUpper> upperList = new ArrayList<>();

    @OneToMany(mappedBy = "dayRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DayRecordOuter> outerList = new ArrayList<>();
}

