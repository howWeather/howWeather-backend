package com.howWeather.howWeather_backend.domain.weather.repository;

import com.howWeather.howWeather_backend.domain.weather.entity.Region;
import jdk.jfr.Registered;
import org.springframework.data.jpa.repository.JpaRepository;

@Registered
public interface RegionRepository extends JpaRepository<Region, Long> {
}
