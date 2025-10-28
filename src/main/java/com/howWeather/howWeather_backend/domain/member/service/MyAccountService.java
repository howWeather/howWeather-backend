package com.howWeather.howWeather_backend.domain.member.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.howWeather.howWeather_backend.domain.ai_model.dto.*;
import com.howWeather.howWeather_backend.domain.ai_model.entity.ClothingRecommendation;
import com.howWeather.howWeather_backend.domain.ai_model.repository.ClothingRecommendationRepository;
import com.howWeather.howWeather_backend.domain.ai_model.schedular.DailyCombinationScheduler;
import com.howWeather.howWeather_backend.domain.ai_model.service.AiInternalService;
import com.howWeather.howWeather_backend.domain.ai_model.service.RecommendationService;
import com.howWeather.howWeather_backend.domain.member.dto.NicknameDto;
import com.howWeather.howWeather_backend.domain.member.dto.ProfileChangeIntDto;
import com.howWeather.howWeather_backend.domain.member.dto.ProfileDto;
import com.howWeather.howWeather_backend.domain.member.dto.RegionDto;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.member.repository.MemberRepository;
import com.howWeather.howWeather_backend.domain.weather.entity.Region;
import com.howWeather.howWeather_backend.domain.weather.entity.WeatherForecast;
import com.howWeather.howWeather_backend.domain.weather.repository.RegionRepository;
import com.howWeather.howWeather_backend.domain.weather.repository.WeatherForecastRepository;
import com.howWeather.howWeather_backend.global.cipher.AESCipher;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyAccountService {
    private final EntityManager entityManager;
    private final WeatherForecastRepository weatherForecastRepository;
    private final MemberRepository memberRepository;
    private final ClothingRecommendationRepository recommendationRepository;
    private final DailyCombinationScheduler dailyCombinationScheduler;
    private final AiInternalService aiInternalService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final RegionRepository regionRepository;
    private final AESCipher aesCipher;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Transactional(readOnly = true)
    public ProfileDto getProfile(Member member) {
        try {
            return ProfileDto.builder()
                    .loginId(member.getLoginId())
                    .email(member.getEmail())
                    .ageGroup(member.getAgeGroup())
                    .nickname(member.getNickname())
                    .bodyType(member.getBodyType())
                    .constitution(member.getConstitution())
                    .gender(member.getGender())
                    .build();

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("프로필 조회 중 에러 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "프로필 조회 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public void updateGender(Member member, ProfileChangeIntDto profileChangeDto) {
        try {
            validateIntData(profileChangeDto.getData(), 1, 2);
            if (profileChangeDto.getData() != null) {
                Member persistedMember = memberRepository.findById(member.getId())
                        .orElseThrow(() -> new CustomException(ErrorCode.ID_NOT_FOUND, "회원 정보를 찾을 수 없습니다."));
                persistedMember.changeGender(profileChangeDto.getData());
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("성별 수정 중 에러 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "성별 수정 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public void updateAgeGroup(Member member, ProfileChangeIntDto profileChangeDto) {
        try {
            validateIntData(profileChangeDto.getData(), 1, 3);
            if (profileChangeDto.getData() != null) {
                Member persistedMember = memberRepository.findById(member.getId())
                        .orElseThrow(() -> new CustomException(ErrorCode.ID_NOT_FOUND, "회원 정보를 찾을 수 없습니다."));
                persistedMember.changeAgeGroup(profileChangeDto.getData());
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("연령대 수정 중 에러 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "연령대 수정 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public void updateNickname(Member member, NicknameDto nicknameDto) {
        try {
            if (nicknameDto.getData() != null) {
                Member persistedMember = memberRepository.findById(member.getId())
                        .orElseThrow(() -> new CustomException(ErrorCode.ID_NOT_FOUND, "회원 정보를 찾을 수 없습니다."));
                persistedMember.changeNickname(nicknameDto.getData());
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("닉네임 수정 중 에러 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "닉네임 수정 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public void updateConstitution(Member member, ProfileChangeIntDto profileChangeDto) {
        try {
            validateIntData(profileChangeDto.getData(), 1, 3);
            if (profileChangeDto.getData() != null) {
                Member persistedMember = memberRepository.findById(member.getId())
                        .orElseThrow(() -> new CustomException(ErrorCode.ID_NOT_FOUND, "회원 정보를 찾을 수 없습니다."));
                persistedMember.changeConstitution(profileChangeDto.getData());
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("체질 수정 중 에러 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "체질 수정 중 오류가 발생했습니다.");
        }
    }

    private void validateIntData(Integer data, int start, int end) {
        if (data < start || data > end) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "유효하지 않은 입력값입니다.");
        }
    }

    @Transactional(readOnly = true)
    public String getLocation(Member member) {
        try {
            String region = member.getRegionName();

            if (region == null || region.trim().isEmpty())
                return "서울특별시 용산구";

            return region;

        } catch (CustomException e) {
            throw e;
        }catch (Exception e) {
            log.error("지역 조회 중 에러 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "지역 조회 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public void updateLocation(Member member, RegionDto regionDto) {
        try {
            validateTimeForRegionChange();
            validateRegion(regionDto.getRegionName());

            Member persistedMember = findMemberOrThrow(member.getId());
            String oldRegionName = persistedMember.getRegionName();
            String newRegionName = regionDto.getRegionName();

            if (shouldUpdateRegion(oldRegionName, newRegionName)) {
                updateRegionCount(persistedMember, oldRegionName, newRegionName);
                persistedMember.changeRegion(newRegionName);

                runDailyCombinationScheduler(persistedMember);
                pushPredictionForSingleMember(persistedMember, newRegionName);
            }

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("지역 수정 중 에러 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "지역 수정 중 오류가 발생했습니다.");
        }
    }

    /**
     * 단일 회원에 대해 AI 예측을 요청하고 결과를 저장합니다.
     * 응답이 없거나 처리 실패 시 Fallback 로직(지역명 업데이트)을 시도합니다.
     * @param member 대상 회원 객체
     * @param region 새로운 지역명
     */
    private void pushPredictionForSingleMember(Member member, String region) {
        Long memberId = member.getId(); // memberId 변수 사용
        try {
            if (member.getCloset() == null) {
                log.warn("[AI 예측 생략] memberId={} 클로젯 정보 없음", memberId);
                return;
            }

            AiPredictionRequestDto dto = aiInternalService.makePredictRequest(member);
            if (dto == null) {
                log.warn("[AI 예측 데이터 없음] memberId={}", memberId);
                // AI 요청 데이터 생성 실패 시 Fallback 실행
                handleFallbackForMissingResponse(memberId, region);
                return;
            }

            // 날씨 정보 정렬
            List<WeatherPredictDto> sortedWeather = Optional.ofNullable(dto.getWeatherForecast()).orElseGet(Collections::emptyList)
                    .stream()
                    .sorted(Comparator.comparingInt(WeatherPredictDto::getHour))
                    .toList();
            dto.setWeatherForecast(sortedWeather);

            log.info("[AI 예측 DTO 확인] memberId={}, bodyType={}, weatherForecast={}, clothingCombinations={}",
                    memberId, dto.getBodyTypeLabel(), dto.getWeatherForecast(), dto.getClothingCombinations());

            // 데이터 암호화
            Map<String, String> encryptedData = encryptPredictionData(List.of(dto));
            if (encryptedData == null || encryptedData.isEmpty()) {
                 log.warn("[AI 예측 생략] memberId={} 데이터 암호화 실패 또는 결과 없음", memberId);
                 // 암호화 실패 시 Fallback 실행
                 handleFallbackForMissingResponse(memberId, region);
                 return;
            }

            // AI 서버 요청
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(encryptedData, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(aiServerUrl, requestEntity, Map.class);

            // 응답 상태 확인
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("[AI 서버 응답 실패] memberId={}, 상태코드={}, 본문={}",
                        memberId, response.getStatusCode(), response.getBody());
                // AI 서버 응답 실패 시 Fallback 실행
                handleFallbackForMissingResponse(memberId, region);
                return;
            }

            // 응답 본문 확인
            Map<String, String> encryptedResponseData = response.getBody();
            log.info("[AI 서버 응답 본문] memberId={}, body={}", memberId, encryptedResponseData);
            if (encryptedResponseData == null || encryptedResponseData.isEmpty()) {
                log.warn("[AI 서버 응답 없음] memberId={}, 응답 본문 null/empty.", memberId);
                // 응답 본문 없을 시 Fallback 실행
                handleFallbackForMissingResponse(memberId, region);
                return;
            }

            saveRecommendationsInternal(encryptedResponseData, memberId, region); // memberId 추가

        } catch (Exception e) {
            // AI 예측 요청/처리 중 발생한 모든 예외 처리
            log.error("[AI 예측 처리 실패] memberId={}, message={}", memberId, e.getMessage(), e);
            // ✨ 예측 처리 자체 실패 시에도 Fallback 실행 ✨
            handleFallbackForMissingResponse(memberId, region);
        }
    }

    /**
     * 단일 회원에 대해 스케줄러 AI 예측 전송 로직 재사용
     */
    private void pushOldPredictionForSingleMember(Member member, String region) {
        try {
            if (member.getCloset() == null) {
                log.warn("[AI 예측 생략] memberId={} 클로젯 정보 없음", member.getId());
                return;
            }

            AiPredictionRequestDto dto = aiInternalService.makePredictRequest(member);
            if (dto == null) {
                log.warn("[AI 예측 데이터 없음] memberId={}", member.getId());
                return;
            }

            List<WeatherPredictDto> sortedWeather = dto.getWeatherForecast().stream()
                    .sorted(Comparator.comparingInt(WeatherPredictDto::getHour))
                    .toList();
            dto.setWeatherForecast(sortedWeather);

            log.info("[AI 예측 DTO 확인] memberId={}, bodyType={}, weatherForecast={}, clothingCombinations={}",
                    member.getId(),
                    dto.getBodyTypeLabel(),
                    dto.getWeatherForecast(),
                    dto.getClothingCombinations());

            Map<String, String> encryptedData = encryptPredictionData(List.of(dto));
            if (encryptedData == null || encryptedData.isEmpty()) return;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(encryptedData, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(aiServerUrl, requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[AI 예측 전송 완료] memberId={}, 응답={}", member.getId(), response.getStatusCode());
            } else {
                log.warn("[AI 서버 응답 실패] memberId={}, 상태코드={}, 본문={}",
                        member.getId(), response.getStatusCode(), response.getBody());
                return;
            }

            Map<String, String> encryptedResponseData = response.getBody();

            log.info("[AI 서버 응답 본문] memberId={}, body={}", member.getId(), encryptedResponseData);
            if (encryptedResponseData == null || encryptedResponseData.isEmpty()) {
                log.warn("[AI 서버 응답 없음] memberId={}, 응답 본문이 null이거나 비어있습니다.", member.getId());
                return;
            }
            saveRecommendationsInternal(encryptedResponseData, region);

        } catch (Exception e) {
            log.error("[AI 예측 처리 실패] memberId={}, message={}", member.getId(), e.getMessage(), e);
        }
    }

    private void validateTimeForRegionChange() {
        LocalTime now = LocalTime.now();
        if (!now.isBefore(LocalTime.of(4, 0)) && now.isBefore(LocalTime.of(7, 0))) {
            throw new CustomException(ErrorCode.TIME_RESTRICTED_FOR_REGION_CHANGE);
        }
    }

    private void validateRegion(String regionName) {
        if (regionName == null || regionName.isBlank()) {
            throw new CustomException(ErrorCode.REGION_NOT_FOUND);
        }
    }

    private Member findMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.ID_NOT_FOUND, "회원 정보를 찾을 수 없습니다."));
    }

    private boolean shouldUpdateRegion(String oldRegionName, String newRegionName) {
        return oldRegionName == null || !oldRegionName.equals(newRegionName);
    }

    private void updateRegionCount(Member member, String oldRegionName, String newRegionName) {
        if (oldRegionName != null) {
            regionRepository.findByName(oldRegionName).ifPresent(Region::decrementCurrentUserCount);
        }
        Region newRegion = regionRepository.findByName(newRegionName)
                .orElseThrow(() -> new CustomException(ErrorCode.REGION_NOT_FOUND));
        newRegion.incrementCurrentUserCount();
    }

    private void runDailyCombinationScheduler(Member member) {
        try {
            dailyCombinationScheduler.refreshDailyCombinations(member);
        } catch (Exception e) {
            log.error("[스케줄러 호출 실패] memberId={}, message={}", member.getId(), e.getMessage(), e);
        }
    }

    private Map<String, String> encryptPredictionData(List<AiPredictionRequestDto> dtos) {
        try {
            String jsonData = objectMapper.writeValueAsString(dtos);
            return aesCipher.encrypt(jsonData);
        } catch (Exception e) {
            log.error("[암호화 실패] message={}", e.getMessage(), e);
            return Map.of();
        }
    }

    /**
     * AI 서버로부터 받은 암호화된 추천 데이터를 복호화하고 저장합니다.
     * JSON 파싱 오류 발생 시, 에러 로그만 남기고 마치 'AI 결과 없음' 시나리오처럼
     * 기존 데이터의 지역명을 업데이트하고 정상 종료합니다.
     *
     * @param encryptedData 암호화된 데이터 Map ("iv", "payload" 포함)
     * @param memberId      대상 사용자 ID (Fallback 시 사용)
     * @param newRegionName 업데이트할 새로운 지역명
     */
    @Transactional
    public void saveRecommendationsInternal(Map<String, String> encryptedData, Long memberId, String newRegionName) { // memberId 파라미터 추가
        // --- 1. 입력 데이터 기본 유효성 검사 ---
        if (encryptedData == null || encryptedData.isEmpty()) {
            log.warn("[saveInternal] memberId={} 전달된 데이터 맵 null/empty. 처리 중단.", memberId);
            return;
        }
        if (!encryptedData.containsKey("iv") || !encryptedData.containsKey("payload")) {
            log.warn("[saveInternal] memberId={} 필수 키 'iv'/'payload' 없음. 응답: {}. 처리 중단.", memberId, encryptedData);
            return;
        }
        String iv = encryptedData.get("iv"); String payload = encryptedData.get("payload");
        if (iv == null || iv.isBlank() || payload == null || payload.isBlank()) {
            log.warn("[saveInternal] memberId={} 'iv'/'payload' 값 null/blank. 응답: {}. 처리 중단.", memberId, encryptedData);
            return;
        }

        String decryptedJson = null; // 로그용 변수
        boolean errorOccurred = false; // 에러 발생 여부 플래그
        boolean dataProcessed = false; // DB 변경 발생 여부 플래그

        try {
            // --- 2. 복호화 ---
            log.info("[saveInternal] memberId={} 복호화 시도...", memberId);
            decryptedJson = aesCipher.decrypt(encryptedData);
            log.debug("[saveInternal] memberId={} 복호화된 JSON: {}", memberId, decryptedJson);

            // --- 3. JSON 파싱 시도 (별도 try-catch) ---
            ModelClothingRecommendationDto[] dtoArray;
            try {
                if (decryptedJson.startsWith("\"") && decryptedJson.endsWith("\"")) {
                    decryptedJson = objectMapper.readValue(decryptedJson, String.class);
                }
                dtoArray = objectMapper.readValue(
                        decryptedJson, ModelClothingRecommendationDto[].class);
                log.info("[saveInternal] memberId={} 파싱 성공 ({}개 항목)", memberId, dtoArray.length);

            } catch (Exception parsingException) {
                // ✨ 파싱 실패 시: 로그 남기고 에러 플래그 설정 후 catch 블록 밖으로 나감 ✨
                log.error("[saveInternal] memberId={} JSON 파싱 실패. Fallback 로직 실행 예정. 에러: {}", memberId, parsingException.getMessage());
                if (decryptedJson != null) {
                    log.error("[saveInternal] memberId={} 파싱 실패 시 원본 JSON: {}", memberId, decryptedJson);
                }
                errorOccurred = true; // 에러 발생 플래그 설정
                dtoArray = new ModelClothingRecommendationDto[0]; // 빈 배열로 초기화 (아래 로직 진행 위함)
            }

            // --- 4. 데이터 처리 (파싱 성공 시) 또는 Fallback 실행 ---
            if (!errorOccurred) {
                // 파싱 성공 시 기존 로직 실행
                 LocalDate today = LocalDate.now();
                 for (ModelClothingRecommendationDto dto : dtoArray) {
                     // userId 일치 확인 등 기존 로직 수행...
                     if (!memberId.equals(dto.getUserId())) {
                         log.warn("[saveInternal] DTO userId({}) 불일치 (대상 memberId={}). 건너<0xEB><0x9B><0x84>.", dto.getUserId(), memberId);
                         continue;
                     }
                     Member member = memberRepository.findById(dto.getUserId())
                             .orElseThrow(() -> new CustomException(ErrorCode.ID_NOT_FOUND));

                     List<ModelRecommendationResult> results = Optional.ofNullable(dto.getResult()).orElseGet(ArrayList::new);

                     if (!results.isEmpty()) {
                         // 새 추천 결과 저장
                         recommendationRepository.deleteByMemberIdAndDate(member.getId(), today);
                         for (ModelRecommendationResult r : results) {
                             ClothingRecommendation entity = convertToEntityWithBuilder(r, member.getId(), newRegionName);
                             recommendationRepository.save(entity);
                         }
                         log.info("[추천 데이터 저장 완료] memberId={}", member.getId());
                         dataProcessed = true;
                     } else {
                         // AI 결과 비었을 때 기존 데이터 지역명 업데이트
                         dataProcessed = handleEmptyAiResult(member, newRegionName) || dataProcessed;
                     }
                 } // End of for loop
            } else { // ✨ errorOccurred가 true일 때 (파싱 실패 시) ✨
                // Fallback 실행: "AI 결과 없음" 시나리오와 동일하게 기존 데이터 지역명 업데이트 시도
                log.info("[Fallback] memberId={} 파싱 실패로 기존 데이터 지역명 업데이트 시도.", memberId);
                Member member = memberRepository.findById(memberId).orElse(null); // memberId로 Member 조회
                if (member != null) {
                    // handleEmptyAiResult 호출하여 지역명 업데이트
                    dataProcessed = handleEmptyAiResult(member, newRegionName) || dataProcessed; // 여기도 dataProcessed 업데이트
                } else {
                     log.warn("[Fallback] memberId={} 사용자 정보를 찾을 수 없어 지역명 업데이트 불가.", memberId);
                }
            }

            // DB 변경이 있었을 경우 flush/clear
            if (dataProcessed) {
                recommendationRepository.flush();
                entityManager.clear();
            }

        } catch (CustomException e) {
             // ID_NOT_FOUND 등 CustomException은 그대로 던짐
             log.error("[saveInternal] 처리 중 CustomException 발생. memberId={} message={}", memberId, e.getMessage(), e);
             throw e;
        } catch (Exception e) {
            // 복호화 실패 등 파싱 외의 예상 못한 Exception 처리
            log.error("[saveInternal] 처리 중 예상 못한 Exception 발생. memberId={} message={}", memberId, e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "추천 데이터 처리 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * (Helper) AI 결과가 비었거나 파싱 실패 시 기존 추천 데이터의 지역명만 업데이트하는 로직
     * @return 업데이트 성공(변경 발생) 여부
     */
    private boolean handleEmptyAiResult(Member member, String newRegionName) {
        LocalDate today = LocalDate.now();
        List<ClothingRecommendation> existingData = recommendationRepository.findByMemberIdAndDate(member.getId(), today);
        boolean updated = false; // 업데이트 발생 여부 플래그
        if (!existingData.isEmpty()) {
            for (ClothingRecommendation rec : existingData) {
                // 지역명이 이미 동일하면 업데이트 건너뛰기
                if (newRegionName.equals(rec.getRegionName())) {
                    log.debug("[handleEmptyAiResult] memberId={} 지역명({}) 이미 최신. 업데이트 불필요.", member.getId(), newRegionName);
                    continue; // 다음 레코드로
                }
                // 엔티티 빌더를 사용하여 업데이트 객체 생성 (null 방어 포함)
                ClothingRecommendation updatedEntity = ClothingRecommendation.builder()
                        .id(rec.getId()) // 기존 ID 유지
                        .memberId(rec.getMemberId())
                        .regionName(newRegionName) // 새 지역명 설정
                        .tops(Optional.ofNullable(rec.getTops()).orElseGet(ArrayList::new))
                        .outers(Optional.ofNullable(rec.getOuters()).orElseGet(ArrayList::new))
                        .predictionMap(Optional.ofNullable(rec.getPredictionMap()).orElseGet(HashMap::new))
                        .date(today) // 날짜 유지
                        .build();
                recommendationRepository.save(updatedEntity); // 업데이트 실행
                updated = true; // 업데이트 발생 표시
            }
            if (updated) {
                 log.info("[handleEmptyAiResult] 기존 데이터 지역명 업데이트 완료. memberId={}", member.getId());
                 // flush/clear는 호출한 쪽에서 관리
            }
        } else {
            log.info("[handleEmptyAiResult] 업데이트할 기존 데이터 없음. memberId={}", member.getId());
        }
        return updated; // 업데이트 발생 여부 반환
    }

    /**
     * (Helper) AI 서버 응답이 없거나 처리 중 예외 발생 시 Fallback 처리 (기존 데이터 지역명 업데이트)
     * @param memberId 사용자 ID
     * @param newRegionName 새로운 지역명
     */
    private void handleFallbackForMissingResponse(Long memberId, String newRegionName) {
        try {
            log.warn("[Fallback 시도] memberId={} AI 응답 없거나 처리 실패. 기존 데이터 지역명 업데이트 시도.", memberId);
            Member member = memberRepository.findById(memberId).orElse(null);
            if (member != null) {
                boolean updated = handleEmptyAiResult(member, newRegionName); // 기존 헬퍼 재사용
                if (updated) { 
                    recommendationRepository.flush(); // DB 반영
                    entityManager.clear();        // 영속성 컨텍스트 초기화
                    log.info("[Fallback 시도] memberId={} 기존 데이터 지역명 업데이트 성공.", memberId);
                } else {
                     log.info("[Fallback 시도] memberId={} 기존 데이터 지역명 업데이트 불필요(이미 최신) 또는 대상 없음.", memberId);
                }
            } else {
                 log.warn("[Fallback 실패] memberId={} 사용자 정보를 찾을 수 없음.", memberId);
            }
        } catch (Exception fallbackEx) {
            // Fallback 로직 자체 실패 시 로그만 남김 (예외 숨김)
            log.error("[Fallback 실패] memberId={} 기존 데이터 업데이트 중 오류: {}", memberId, fallbackEx.getMessage(), fallbackEx);
        }
    }
    
    @Transactional
    public void saveOldRecommendationsInternal(Map<String, String> encryptedData, String newRegionName) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            log.warn("[추천 데이터 처리 중단] 전달된 데이터 맵이 null이거나 비어있습니다.");
            return;
        }

        if (!encryptedData.containsKey("iv") || !encryptedData.containsKey("payload")) {
            log.warn("[추천 데이터 처리 중단] AI 응답 Map에 복호화에 필요한 'iv' 또는 'payload' 키가 없습니다. 응답: {}", encryptedData);
            return;
        }

        String iv = encryptedData.get("iv");
        String payload = encryptedData.get("payload");
        if (iv == null || iv.isBlank() || payload == null || payload.isBlank()) {
            log.warn("[추천 데이터 처리 중단] AI 응답 Map의 'iv' 또는 'payload' 값이 null이거나 비어있습니다. 응답: {}", encryptedData);
            return;
        }

        try {
            String decryptedJson = aesCipher.decrypt(encryptedData);

            if (decryptedJson.startsWith("\"") && decryptedJson.endsWith("\"")) {
                decryptedJson = objectMapper.readValue(decryptedJson, String.class);
            }

            ModelClothingRecommendationDto[] dtoArray = objectMapper.readValue(
                    decryptedJson, ModelClothingRecommendationDto[].class);

            for (ModelClothingRecommendationDto dto : dtoArray) {
                Member member = memberRepository.findById(dto.getUserId())
                        .orElseThrow(() -> new CustomException(ErrorCode.ID_NOT_FOUND));

                List<ModelRecommendationResult> results = Optional.ofNullable(dto.getResult())
                        .orElseGet(ArrayList::new);

                if (!results.isEmpty()) {
                    recommendationRepository.deleteByMemberIdAndDate(member.getId(), LocalDate.now());

                    for (ModelRecommendationResult r : results) {
                        ClothingRecommendation entity = convertToEntityWithBuilder(r, member.getId(), newRegionName);
                        recommendationRepository.save(entity);
                    }

                    log.info("[추천 데이터 저장 완료] memberId={}", member.getId());

                    recommendationRepository.flush();
                    entityManager.clear();
                } else {
                    List<ClothingRecommendation> existingData =
                            recommendationRepository.findByMemberIdAndDate(member.getId(), LocalDate.now());

                    if (!existingData.isEmpty()) {
                        for (ClothingRecommendation rec : existingData) {
                            Map<String, Integer> updatedPrediction = new HashMap<>(rec.getPredictionMap());

                            ClothingRecommendation updatedEntity = ClothingRecommendation.builder()
                                    .id(rec.getId())
                                    .memberId(rec.getMemberId())
                                    .regionName(newRegionName)
                                    .tops(new ArrayList<>(rec.getTops()))
                                    .outers(new ArrayList<>(rec.getOuters()))
                                    .predictionMap(updatedPrediction)
                                    .date(LocalDate.now())
                                    .build();

                            recommendationRepository.save(updatedEntity);
                        }
                        recommendationRepository.flush();
                        entityManager.clear();

                        log.info("[기존 데이터 지역명 업데이트 완료] memberId={}", member.getId());
                    } else {
                        log.info("[기존 데이터 없음] AI 결과가 비어있고 업데이트할 기존 데이터도 없습니다. memberId={}", member.getId());
                    }
                }
            }

        } catch (CustomException e) {
            log.error("[추천 데이터 처리 실패] KNOWN_ERROR - memberId={} message={}", encryptedData.get("userId"), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[추천 데이터 처리 실패] UNKNOWN_ERROR - message={}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "지역명 업데이트 중 오류가 발생했습니다.");
        }
    }

    private ClothingRecommendation convertToEntityWithBuilder(ModelRecommendationResult dto, Long memberId, String regionName) {
        return ClothingRecommendation.builder()
                .memberId(memberId)
                .regionName(regionName)
                .tops(new ArrayList<>(dto.getTops()))
                .outers(new ArrayList<>(dto.getOuters()))
                .predictionMap(new HashMap<>(dto.getPredictFeeling()))
                .date(LocalDate.now())
                .build();
    }

    private List<WeatherPredictDto> getWeatherForecastForRegion(String regionName) {
        List<Integer> targetHours = List.of(9, 12, 15, 18, 21);
        LocalDate today = LocalDate.now();

        List<WeatherForecast> forecasts = weatherForecastRepository
                .findByRegionNameAndForecastDateAndHourIn(regionName, today, targetHours);

        if (forecasts.isEmpty()) {
            LocalDate yesterday = today.minusDays(1);
            log.warn("[날씨 조회 재시도] 지역 {}에 대해 오늘({}) 데이터가 없어 어제({}) 날짜로 재시도합니다.",
                    regionName, today, yesterday);

            forecasts = weatherForecastRepository
                    .findByRegionNameAndForecastDateAndHourIn(regionName, yesterday, targetHours);

            if (forecasts.isEmpty()) {
                log.error("[날씨 조회 최종 실패] 지역 {}에 대해 어제, 오늘 모두 예보 데이터가 없습니다.", regionName);
            }
        }

        return forecasts.stream()
                .map(f -> WeatherPredictDto.builder()
                        .hour(f.getHour())
                        .temperature(f.getTemperature())
                        .humidity(f.getHumidity())
                        .windSpeed(f.getWindSpeed())
                        .precipitation(f.getPrecipitation())
                        .cloudAmount(f.getCloudAmount())
                        .feelsLike(f.getFeelsLike())
                        .build())
                .collect(Collectors.toList());
    }
}
