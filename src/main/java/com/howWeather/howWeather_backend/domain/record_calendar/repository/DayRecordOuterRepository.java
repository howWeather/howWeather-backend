package com.howWeather.howWeather_backend.domain.record_calendar.repository;

import com.howWeather.howWeather_backend.domain.record_calendar.entity.DayRecordOuter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DayRecordOuterRepository extends JpaRepository<DayRecordOuter, Long> {
    List<DayRecordOuter> findByDayRecordId(Long dayRecordId);
}
