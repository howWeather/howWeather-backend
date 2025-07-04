package com.howWeather.howWeather_backend.domain.weather.repository;

import com.howWeather.howWeather_backend.domain.weather.entity.Region;
import jdk.jfr.Registered;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

@Registered
public interface RegionRepository extends JpaRepository<Region, Long> {
    boolean existsByName(String name);

    Optional<Region> findByName(String name);

    @Query("SELECT r.name FROM Region r")
    List<String> findAllRegionNames();

    List<Region> findByCurrentUserCountGreaterThan(int count);
}
