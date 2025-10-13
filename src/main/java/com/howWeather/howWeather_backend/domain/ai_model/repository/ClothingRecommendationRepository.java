package com.howWeather.howWeather_backend.domain.ai_model.repository;

import com.howWeather.howWeather_backend.domain.ai_model.entity.ClothingRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ClothingRecommendationRepository extends JpaRepository<ClothingRecommendation, Long> {
    List<ClothingRecommendation> findByMemberIdAndDate(Long memberId, LocalDate date);

    void deleteByDateBefore(LocalDate date);

    @Transactional
    void deleteByMemberIdAndDate(Long memberId, LocalDate date);
}
