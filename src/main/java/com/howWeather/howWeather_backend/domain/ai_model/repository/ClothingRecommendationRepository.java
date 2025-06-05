package com.howWeather.howWeather_backend.domain.ai_model.repository;

import com.howWeather.howWeather_backend.domain.ai_model.entity.ClothingRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClothingRecommendationRepository extends JpaRepository<ClothingRecommendation, Long> {
}
