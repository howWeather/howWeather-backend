package com.howWeather.howWeather_backend.domain.record_calendar.repository;

import com.howWeather.howWeather_backend.domain.record_calendar.entity.DayRecord;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DayRecordRepository extends JpaRepository<DayRecord, Long> {
    boolean existsByMemberAndDateAndTimeSlot(Member member, LocalDate date, int timeSlot);

    List<DayRecord> findByMemberAndDate(Member member, LocalDate date);

    List<DayRecord> findByMemberAndDateBetween(Member member, LocalDate startDate, LocalDate endDate);
}
