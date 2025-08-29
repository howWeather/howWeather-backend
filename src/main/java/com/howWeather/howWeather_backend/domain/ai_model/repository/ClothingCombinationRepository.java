package com.howWeather.howWeather_backend.domain.ai_model.repository;

import com.howWeather.howWeather_backend.domain.ai_model.entity.ClothingCombination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClothingCombinationRepository extends JpaRepository<ClothingCombination, Long> {
    Optional<ClothingCombination> findByMemberId(Long memberId);
}
