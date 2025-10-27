package com.howWeather.howWeather_backend.domain.weather.repository;

import com.howWeather.howWeather_backend.domain.weather.entity.WeatherForecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WeatherForecastRepository extends JpaRepository<WeatherForecast, Long> {
    void deleteByForecastDateBefore(LocalDate date);

    List<WeatherForecast> findByRegionNameAndForecastDate(String regionName, LocalDate now);

    List<WeatherForecast> findByRegionNameAndForecastDateAndHourInOrderByCreatedAtDesc(
            String regionName,
            LocalDate forecastDate,
            List<Integer> hours
    );

    void deleteByForecastDateGreaterThanEqual(LocalDate baseDate);

    List<WeatherForecast> findByRegionNameAndForecastDateAndHourIn(String regionName, LocalDate now, List<Integer> targetHours);

    List<WeatherForecast> findByRegionNameAndForecastDateAndHourInOrderByHourAsc(String regionName, LocalDate forecastDate, List<Integer> targetHours);
}
