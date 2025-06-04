package com.howWeather.howWeather_backend.domain.weather.repository;

import com.howWeather.howWeather_backend.domain.weather.entity.WeatherForecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface WeatherForecastRepository extends JpaRepository<WeatherForecast, Long> {
    void deleteByForecastDate(LocalDate forecastDate);
}
