package com.howWeather.howWeather_backend.domain.ai_model.service;

import com.howWeather.howWeather_backend.domain.ai_model.dto.ClothingRecommendationDto;
import com.howWeather.howWeather_backend.domain.ai_model.dto.RecommendationResult;
import com.howWeather.howWeather_backend.domain.ai_model.entity.ClothingRecommendation;
import com.howWeather.howWeather_backend.domain.ai_model.repository.ClothingRecommendationRepository;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@AllArgsConstructor
public class RecommendationService {
    private final ClothingRecommendationRepository clothingRecommendationRepository;

    @Transactional
    public void save(ClothingRecommendationDto dto) {
        try {
            for (RecommendationResult result : dto.getResult()) {
                ClothingRecommendation recommendation = ClothingRecommendation.builder()
                        .memberId(dto.getUserId())
                        .tops(result.getTops())
                        .outers(result.getOuters())
                        .predictionMap(result.getPredictFeeling())
                        .date(LocalDate.now())
                        .build();

                clothingRecommendationRepository.save(recommendation);
            }
        } catch (Exception e) {
            log.error("모델로부터 예측 결과를 받아오던 중 중 알 수 없는 에러 발생", e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "예측 결과를 AI로부터 받아오던 중 오류가 발생했습니다.");
        }
    }
}
