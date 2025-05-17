package com.howWeather.howWeather_backend.domain.record_calendar.repository;

import com.howWeather.howWeather_backend.domain.record_calendar.entity.DayRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DayRecordRepository extends JpaRepository<DayRecord, Long> {
}
