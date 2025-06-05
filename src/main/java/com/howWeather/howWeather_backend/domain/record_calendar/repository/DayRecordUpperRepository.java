package com.howWeather.howWeather_backend.domain.record_calendar.repository;

import com.howWeather.howWeather_backend.domain.record_calendar.entity.DayRecordUpper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DayRecordUpperRepository extends JpaRepository<DayRecordUpper, Long> {
    List<DayRecordUpper> findByDayRecordId(Long dayRecordId);
}
