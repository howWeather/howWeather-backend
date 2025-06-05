package com.howWeather.howWeather_backend.domain.ai_model.api;

import com.howWeather.howWeather_backend.domain.ai_model.dto.AiPredictionRequestDto;
import com.howWeather.howWeather_backend.domain.ai_model.dto.ClothingRecommendationDto;
import com.howWeather.howWeather_backend.domain.ai_model.service.AiInternalService;
import com.howWeather.howWeather_backend.domain.ai_model.service.RecommendationService;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.member.repository.MemberRepository;
import com.howWeather.howWeather_backend.global.Response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal-api/model")
public class AiInternalController {
    private final AiInternalService aiInternalService;
    private final MemberRepository memberRepository;
    private final RecommendationService recommendationService;

    @PostMapping("/prediction")
    public ResponseEntity<List<AiPredictionRequestDto>> sendAllUsersPredictionData() {
        try {
            List<Member> members = memberRepository.findAllByIsDeletedFalse();

            List<AiPredictionRequestDto> allDtos = members.stream()
                    .filter(member -> member.getCloset() != null)
                    .map(aiInternalService::makePredictRequest)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(allDtos);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @PostMapping("/recommendation")
    public ResponseEntity<ApiResponse<String>> saveRecommendations(@RequestBody ClothingRecommendationDto dto) {
        recommendationService.save(dto);
        return ApiResponse.success(HttpStatus.OK, "예측 결과를 성공적으로 저장했습니다.");
    }

}
