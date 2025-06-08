package com.howWeather.howWeather_backend.domain.ai_model.api;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.howWeather.howWeather_backend.global.cipher.AESCipher;
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
import java.util.Map;
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
    private final AESCipher aesCipher;
    private final ObjectMapper objectMapper;

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

    @PostMapping("/aes-prediction")
    public ResponseEntity<Map<String, String>> sendAllUsersPredictionDataSecure() {
        try {
            List<Member> members = memberRepository.findAllByIsDeletedFalse();

            List<AiPredictionRequestDto> allDtos = members.stream()
                    .filter(member -> member.getCloset() != null)
                    .map(aiInternalService::makePredictRequest)
                    .collect(Collectors.toList());

            Map<String, String> encrypted = encryptPredictionData(allDtos);
            return ResponseEntity.ok(encrypted);

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

    private Map<String, String> encryptPredictionData(List<AiPredictionRequestDto> dtos) {
        try {
            String jsonData = objectMapper.writeValueAsString(dtos);
            return aesCipher.encrypt(jsonData); // {"iv": "...", "payload": "..."}
        } catch (Exception e) {
            log.error("예측 데이터 암호화 실패: {}", e.getMessage());
            return null;
        }
    }
}
