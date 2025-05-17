package com.howWeather.howWeather_backend.domain.record_calendar.repository;

import com.howWeather.howWeather_backend.domain.record_calendar.entity.DayRecordUpper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DayRecordUpperRepository extends JpaRepository<DayRecordUpper, Long> {
}
