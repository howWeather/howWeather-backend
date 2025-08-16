package com.howWeather.howWeather_backend.domain.closet.repository;

import com.howWeather.howWeather_backend.domain.closet.entity.Closet;
import com.howWeather.howWeather_backend.domain.closet.entity.Upper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UpperRepository extends JpaRepository<Upper, Long> {
    Optional<Upper> findByIdAndCloset(Long id, Closet closet);
    List<Upper> findByWarmthIndexIsNull();
}
