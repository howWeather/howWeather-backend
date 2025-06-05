package com.howWeather.howWeather_backend.domain.ai_model.api;

import com.howWeather.howWeather_backend.domain.ai_model.dto.AiPredictionRequestDto;
import com.howWeather.howWeather_backend.domain.ai_model.dto.HistoryRequestDto;
import com.howWeather.howWeather_backend.domain.ai_model.dto.ModelClothingRecommendationDto;
import com.howWeather.howWeather_backend.domain.ai_model.service.AiInternalService;
import com.howWeather.howWeather_backend.domain.ai_model.service.RecommendationService;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.member.repository.MemberRepository;
import com.howWeather.howWeather_backend.domain.record_calendar.dto.RecordForModelDto;
import com.howWeather.howWeather_backend.domain.record_calendar.service.RecordCalendarService;
import com.howWeather.howWeather_backend.global.Response.ApiResponse;
import jakarta.validation.Valid;
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
    private final RecordCalendarService recordCalendarService;

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
    public ResponseEntity<ApiResponse<String>> saveRecommendations(@RequestBody ModelClothingRecommendationDto dto) {
        recommendationService.save(dto);
        return ApiResponse.success(HttpStatus.OK, "예측 결과를 성공적으로 저장했습니다.");
    }

    @PostMapping("/history")
    public ResponseEntity<ApiResponse<List<RecordForModelDto>>> getSimilarHistory(@RequestBody HistoryRequestDto dto) {
        dto.validate();
        List<RecordForModelDto> result = recordCalendarService.getMemberHistory(dto);
        return ApiResponse.success(HttpStatus.OK, result);
    }
}
