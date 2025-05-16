package com.howWeather.howWeather_backend.domain.record_calendar.entity;

import com.howWeather.howWeather_backend.domain.closet.entity.Upper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayRecordUpper {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "day_record_id")
    private DayRecord dayRecord;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "upper_id")
    private Upper upper;
}