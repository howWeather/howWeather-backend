package com.howWeather.howWeather_backend.domain.record_calendar.entity;

import com.howWeather.howWeather_backend.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class DayRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private int timeSlot;

    private int feeling;

    private double temperature;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "dayRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DayRecordUpper> upperList = new ArrayList<>();

    @OneToMany(mappedBy = "dayRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DayRecordOuter> outerList = new ArrayList<>();

    public void assignMember(Member member) {
        this.member = member;
    }

    public void addUpper(DayRecordUpper dayRecordUpper) {
        upperList.add(dayRecordUpper);
        dayRecordUpper.setDayRecordInternal(this);
    }

    public void addUppers(List<DayRecordUpper> dayRecordUppers) {
        dayRecordUppers.forEach(this::addUpper);
    }

    public void addOuter(DayRecordOuter dayRecordOuter) {
        outerList.add(dayRecordOuter);
        dayRecordOuter.setDayRecordInternal(this);
    }

    public void addOuters(List<DayRecordOuter> dayRecordOuters) {
        dayRecordOuters.forEach(this::addOuter);
    }
}

