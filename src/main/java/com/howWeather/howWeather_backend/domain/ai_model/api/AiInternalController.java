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
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ExceptionDto;
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

    @PostMapping("/aes-recommendation")
    public ResponseEntity<ApiResponse<String>> saveRecommendations(@RequestBody Map<String, String> encryptedBody) {
        try {
            String decryptedJson = aesCipher.decrypt(encryptedBody);

            if (decryptedJson.startsWith("\"") && decryptedJson.endsWith("\"")) {
                decryptedJson = objectMapper.readValue(decryptedJson, String.class);
            }

            ModelClothingRecommendationDto dto = objectMapper.readValue(
                    decryptedJson,
                    ModelClothingRecommendationDto.class
            );

            recommendationService.save(dto);
            return ApiResponse.success(HttpStatus.OK, "예측 결과를 성공적으로 저장했습니다.");
        } catch (CustomException e) {
            log.error("커스텀 예외 발생: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(e.getHttpStatus())
                    .body(ApiResponse.fail(e));
        } catch (Exception e) {
            log.error("예측 결과 복호화 또는 저장 실패: {}", e.getMessage(), e);
            ApiResponse<String> errorResponse = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST,
                    false,
                    null,
                    new ExceptionDto("UNKNOWN_ERROR", e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/aes-history")
    public ResponseEntity<Map<String, String>> getSimilarHistoryEncrypted(@RequestBody HistoryRequestDto dto) {
        try {
            dto.validate();
            List<RecordForModelDto> result = recordCalendarService.getMemberHistory(dto);
            String json = objectMapper.writeValueAsString(result);
            Map<String, String> encrypted = aesCipher.encrypt(json);
            return ResponseEntity.ok(encrypted);
        } catch (Exception e) {
            log.error("히스토리 암호화 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, String> encryptPredictionData(List<AiPredictionRequestDto> dtos) {
        try {
            String jsonData = objectMapper.writeValueAsString(dtos);
            return aesCipher.encrypt(jsonData);
        } catch (Exception e) {
            log.error("예측 데이터 암호화 실패: {}", e.getMessage());
            return null;
        }
    }
    
    // TODO : 암/복호화 API 연결 이후 아래 코드 삭제
//    @PostMapping("/prediction")
//    public ResponseEntity<List<AiPredictionRequestDto>> sendAllUsersPredictionData() {
//        try {
//            List<Member> members = memberRepository.findAllByIsDeletedFalse();
//
//            List<AiPredictionRequestDto> allDtos = members.stream()
//                    .filter(member -> member.getCloset() != null)
//                    .map(aiInternalService::makePredictRequest)
//                    .collect(Collectors.toList());
//
//            return ResponseEntity.ok(allDtos);
//        } catch (Exception e) {
//            log.error(e.getMessage());
//        }
//        return null;
//    }
//
//    @PostMapping("/recommendation")
//    public ResponseEntity<ApiResponse<String>> saveRecommendations(@RequestBody ModelClothingRecommendationDto dto) {
//        recommendationService.save(dto);
//        return ApiResponse.success(HttpStatus.OK, "예측 결과를 성공적으로 저장했습니다.");
//    }
//
//    @PostMapping("/history")
//    public ResponseEntity<ApiResponse<List<RecordForModelDto>>> getSimilarHistory(@RequestBody HistoryRequestDto dto) {
//        dto.validate();
//        List<RecordForModelDto> result = recordCalendarService.getMemberHistory(dto);
//        return ApiResponse.success(HttpStatus.OK, result);
//    }
}
