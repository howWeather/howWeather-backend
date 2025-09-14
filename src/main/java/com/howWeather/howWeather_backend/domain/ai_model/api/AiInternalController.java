package com.howWeather.howWeather_backend.domain.ai_model.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.howWeather.howWeather_backend.domain.ai_model.dto.AiPredictionRequestDto;
import com.howWeather.howWeather_backend.domain.ai_model.dto.HistoryRequestDto;
import com.howWeather.howWeather_backend.domain.ai_model.dto.ModelClothingRecommendationDto;
import com.howWeather.howWeather_backend.domain.ai_model.service.AiInternalService;
import com.howWeather.howWeather_backend.domain.ai_model.service.RecommendationService;
import com.howWeather.howWeather_backend.domain.closet.entity.Closet;
import com.howWeather.howWeather_backend.domain.closet.repository.ClosetRepository;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.member.repository.MemberRepository;
import com.howWeather.howWeather_backend.domain.record_calendar.dto.RecordForModelDto;
import com.howWeather.howWeather_backend.domain.record_calendar.service.RecordCalendarService;
import com.howWeather.howWeather_backend.global.Response.ApiResponse;
import com.howWeather.howWeather_backend.global.cipher.AESCipher;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import com.howWeather.howWeather_backend.global.exception.ExceptionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("${service-api.ai}")
public class AiInternalController {
    private final AiInternalService aiInternalService;
    private final MemberRepository memberRepository;
    private final RecommendationService recommendationService;
    private final RecordCalendarService recordCalendarService;
    private final AESCipher aesCipher;
    private final ObjectMapper objectMapper;

    private final ClosetRepository closetRepository;

    @PostMapping("/aes-prediction")
    public ResponseEntity<Map<String, String>> sendAllUsersPredictionDataSecure() {
        try {
            List<Member> members = memberRepository.findAllByIsDeletedFalse();
            List<AiPredictionRequestDto> allDtos = aiInternalService.makePredictRequestsSafely(members);

            if (allDtos.isEmpty()) {
                log.info("AI 서버로 전송할 데이터가 없습니다.");
                return ResponseEntity.ok(Map.of());
            }

            Map<String, String> encrypted = encryptPredictionData(allDtos);
            return ResponseEntity.ok(encrypted);

        } catch (Exception e) {
            log.error("전체 예측 데이터 암호화 또는 전송 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/aes-recommendation")
    public ResponseEntity<ApiResponse<String>> saveRecommendations(
            @RequestBody Map<String, String> encryptedBody) {
        try {
            String decryptedJson = aesCipher.decrypt(encryptedBody);

            if (decryptedJson.startsWith("\"") && decryptedJson.endsWith("\"")) {
                decryptedJson = objectMapper.readValue(decryptedJson, String.class);
            }

            ModelClothingRecommendationDto dto = objectMapper.readValue(
                    decryptedJson,
                    ModelClothingRecommendationDto.class
            );
            
            Member member = memberRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUNT));

            recommendationService.save(dto, member);

            return ApiResponse.success(HttpStatus.OK, "예측 결과를 성공적으로 저장했습니다.");
        } catch (CustomException e) {
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

    // TODO : 테스트용 추후 삭제
    @GetMapping("/refresh-mark")
    @Transactional
    public ResponseEntity<String> refreshClosetMark() {
        List<Closet> closets = closetRepository.findAll();
        for (Closet closet : closets) {
            closet.markNeedsRefresh();
        }
        log.info("모든 Closet의 needsCombinationRefresh를 true로 초기화 완료");
        return ResponseEntity.ok("모든 Closet의 needsCombinationRefresh가 true로 초기화되었습니다.");
    }

    // TODO : 테스트용 추후 삭제
    @GetMapping("/test-prediction-data")
    public ResponseEntity<List<AiPredictionRequestDto>> getAllUsersPredictionDataForTest() {
        try {
            List<Member> members = memberRepository.findAllByIsDeletedFalse();
            List<AiPredictionRequestDto> allDtos = aiInternalService.makePredictRequestsSafely(members);

            if (allDtos.isEmpty()) {
                log.info("AI 서버로 전송할 테스트 데이터가 없습니다.");
                return ResponseEntity.ok(List.of());
            }

            return ResponseEntity.ok(allDtos);

        } catch (Exception e) {
            log.error("전체 예측 데이터 테스트 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of());
        }
    }
}
