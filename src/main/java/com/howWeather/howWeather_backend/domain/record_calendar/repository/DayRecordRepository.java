package com.howWeather.howWeather_backend.domain.record_calendar.repository;

import com.howWeather.howWeather_backend.domain.record_calendar.entity.DayRecord;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DayRecordRepository extends JpaRepository<DayRecord, Long> {
    boolean existsByMemberAndDateAndTimeSlot(Member member, LocalDate date, int timeSlot);

    List<DayRecord> findByMemberAndDate(Member member, LocalDate date);
    List<DayRecord> findByMemberAndDateBetween(Member member, LocalDate startDate, LocalDate endDate);
    List<DayRecord> findByMemberAndTemperatureBetweenAndDateBetween(
            Member member,
            double low,
            double high,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("SELECT dr.id FROM DayRecord dr " +
            "WHERE dr.member.id = :memberId " +
            "AND dr.temperature BETWEEN :lower AND :upper " +
            "ORDER BY dr.id DESC")
    List<Long> findRecentRecordIdsByMemberIdAndTemperatureRange(
            @Param("memberId") Long memberId,
            @Param("lower") double lower,
            @Param("upper") double upper,
            Pageable pageable
    );

    Optional<Integer> findFeelingById(Long id);
    Optional<Double> findTemperatureById(Long id);
}
