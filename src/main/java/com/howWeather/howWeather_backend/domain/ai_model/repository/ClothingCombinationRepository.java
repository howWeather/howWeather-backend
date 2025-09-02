package com.howWeather.howWeather_backend.domain.ai_model.repository;

import com.howWeather.howWeather_backend.domain.ai_model.entity.ClothingCombination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ClothingCombinationRepository extends JpaRepository<ClothingCombination, Long> {
    Optional<ClothingCombination> findByMemberId(Long memberId);

    @Query("SELECT c.lastModified FROM ClothingCombination c WHERE c.memberId = :memberId")
    LocalDateTime findLastModifiedByMemberId(@Param("memberId") Long memberId);
}
