package com.howWeather.howWeather_backend.domain.closet.repository;

import com.howWeather.howWeather_backend.domain.closet.entity.Closet;
import com.howWeather.howWeather_backend.domain.closet.entity.Outer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OuterRepository extends JpaRepository<Outer, Long> {
    Optional<Outer> findByIdAndCloset(Long id, Closet closet);
    List<Outer> findByWarmthIndexIsNull();
}
