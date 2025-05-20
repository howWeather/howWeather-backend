package com.howWeather.howWeather_backend.domain.record_calendar.entity;

import com.howWeather.howWeather_backend.domain.closet.entity.Outer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class DayRecordOuter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "day_record_id")
    private DayRecord dayRecord;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "outer_id")
    private Outer outer;

    void setDayRecordInternal(DayRecord dayRecord) {
        this.dayRecord = dayRecord;
    }

    void setOuterInternal(Outer outer) {
        this.outer = outer;
    }
}
