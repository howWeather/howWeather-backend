package com.howWeather.howWeather_backend.domain.ai_model.repository;

import com.howWeather.howWeather_backend.domain.ai_model.entity.ClothingRecommendation;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ClothingRecommendationRepository extends JpaRepository<ClothingRecommendation, Long> {
    @QueryHints({ @QueryHint(name = "javax.persistence.cache.retrieveMode", value = "BYPASS") })
    List<ClothingRecommendation> findByMemberIdAndDate(Long memberId, LocalDate date);

    void deleteByDateBefore(LocalDate date);

    @Transactional
    void deleteByMemberIdAndDate(Long memberId, LocalDate date);
}
