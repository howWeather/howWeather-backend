package com.howWeather.howWeather_backend.domain.weather.repository;

import com.howWeather.howWeather_backend.domain.weather.entity.Weather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, Long> {
    void deleteByDate(LocalDate date);
    Optional<Weather> findByRegionNameAndDateAndTimeSlot(String regionName, LocalDate date, int timeSlot);
}
