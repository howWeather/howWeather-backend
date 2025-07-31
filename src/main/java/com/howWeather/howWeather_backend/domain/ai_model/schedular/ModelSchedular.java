package com.howWeather.howWeather_backend.domain.ai_model.schedular;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.howWeather.howWeather_backend.domain.ai_model.dto.AiPredictionRequestDto;
import com.howWeather.howWeather_backend.domain.ai_model.repository.ClothingRecommendationRepository;
import com.howWeather.howWeather_backend.domain.ai_model.service.AiInternalService;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.member.repository.MemberRepository;
import com.howWeather.howWeather_backend.global.cipher.AESCipher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ModelSchedular {
    private final MemberRepository memberRepository;
    private final AiInternalService aiInternalService;
    private final RestTemplate restTemplate;
    private final ClothingRecommendationRepository clothingRecommendationRepository;
    private final AESCipher aesCipher;
    private final ObjectMapper objectMapper;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    /**
     * 매일 새벽 5시에 모델 서버로 예측에 필요한 데이터를 암호화된 형태로 전송합니다.
     */
    @Transactional
    @Scheduled(cron = "0 0 5 * * *")
    public void pushPredictionAESDataToAiServer() {
        try {
            List<Member> members = memberRepository.findAllByIsDeletedFalse();

            List<AiPredictionRequestDto> allDtos = members.stream()
                    .filter(member -> member.getCloset() != null)
                    .map(member -> {
                        try {
                            return aiInternalService.makePredictRequest(member);
                        } catch (Exception e) {
                            log.error("멤버 {} 예측 데이터 생성 실패: {}", member.getId(), e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (allDtos.isEmpty()) {
                log.info("AI 서버로 전송할 데이터가 없습니다.");
                return;
            }

            Map<String, String> encrypted = encryptPredictionData(allDtos);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(encrypted, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(aiServerUrl, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("AI 서버에 암호화된 예측 데이터 전송 완료. 응답: {}", response.getStatusCode());
            } else {
                log.warn("AI 서버 응답 실패. 상태코드: {}, 응답본문: {}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("AI 서버 전송 실패", e);
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

    /**
     * 매일 오전 7시 30분에 오늘 이전의 의상 추천 데이터 및 예측 체감 정보를 삭제합니다.
     */
    @Transactional
    @Scheduled(cron = "0 30 7 * * *")
    public void deleteOldRecommendations() {
        LocalDate today = LocalDate.now();
        clothingRecommendationRepository.deleteByDateBefore(today);
        log.info("오늘 이전의 의상 추천 데이터 및 예측 체감 정보를 모두 삭제했습니다. 기준 날짜: {}", today);
    }
}
