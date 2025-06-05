package com.howWeather.howWeather_backend.domain.weather.repository;

import com.howWeather.howWeather_backend.domain.weather.entity.Weather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM Weather w WHERE w.date <= :date")
    void deleteByDateBeforeEqual(@Param("date") LocalDate date);

    Optional<Weather> findByRegionNameAndDateAndTimeSlot(String regionName, LocalDate date, int timeSlot);
}
