package com.howWeather.howWeather_backend.domain.record_calendar.repository;

import com.howWeather.howWeather_backend.domain.record_calendar.entity.DayRecord;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;

@Repository
public interface DayRecordRepository extends JpaRepository<DayRecord, Long> {
    boolean existsByMemberAndDateAndTimeSlot(Member member, LocalDate date, int timeSlot);
}
