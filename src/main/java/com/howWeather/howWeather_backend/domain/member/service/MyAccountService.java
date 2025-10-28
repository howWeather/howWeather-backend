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

    private void pushPredictionForSingleMember(Member member, String region) {
        log.info("[Service pushPrediction START] memberId={}, region={}", member.getId(), region);
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
            if (encryptedData == null || encryptedData.isEmpty()) {
                log.warn("[Service pushPrediction] 암호화된 예측 데이터가 비어있어 전송 중단. memberId={}", member.getId());
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(encryptedData, headers);

            log.info("[Service pushPrediction] AI 서버({})로 예측 요청 전송 시작. memberId={}", aiServerUrl, member.getId());
            ResponseEntity<Map> response = restTemplate.postForEntity(aiServerUrl, requestEntity, Map.class);
            log.info("[Service pushPrediction] AI 서버 응답 수신 완료. memberId={}, statusCode={}", member.getId(), response.getStatusCode());

            if (!response.getStatusCode().is2xxSuccessful()) {
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


            log.info("[Service pushPrediction] saveRecommendationsInternal 호출 시작. memberId={}", member.getId());
            saveRecommendationsInternal(encryptedResponseData, region);
            log.info("[Service pushPrediction] saveRecommendationsInternal 호출 완료. memberId={}", member.getId());
            log.info("[Service pushPrediction END] memberId={}", member.getId());

        } catch (Exception e) {
            log.error("[Service pushPrediction ERROR] memberId={}, message={}", member.getId(), e.getMessage(), e);

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

    @Transactional
    public void saveRecommendationsInternal(Map<String, String> encryptedData, String newRegionName) {
        log.info("[Service saveInternal START] region={}, encryptedData keys={}, iv_present={}, payload_present={}",
                newRegionName, encryptedData != null ? encryptedData.keySet() : "null",
                encryptedData != null && encryptedData.containsKey("iv"),
                encryptedData != null && encryptedData.containsKey("payload"));

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

        String decryptedJson = null;
        try {
            log.debug("[Service saveInternal] 복호화 시도...");
            decryptedJson = aesCipher.decrypt(encryptedData);
            log.info("[Service saveInternal] 복호화 성공. JSON 길이={}", decryptedJson != null ? decryptedJson.length() : "null");

            if (decryptedJson != null && decryptedJson.startsWith("\"") && decryptedJson.endsWith("\"")) {
                log.debug("[Service saveInternal] 이중 따옴표 제거 처리...");
                decryptedJson = objectMapper.readValue(decryptedJson, String.class);
                log.debug("[Service saveInternal] 이중 따옴표 제거 완료. JSON 길이={}", decryptedJson != null ? decryptedJson.length() : "null");
            }

            log.info("[Service saveInternal] JSON 파싱 시도 (ModelClothingRecommendationDto[])...");
            ModelClothingRecommendationDto[] dtoArray = objectMapper.readValue(
                    decryptedJson, ModelClothingRecommendationDto[].class);
            log.info("[Service saveInternal] JSON 파싱 성공. DTO 개수={}", dtoArray != null ? dtoArray.length : 0);

            for (ModelClothingRecommendationDto dto : dtoArray) {
                Member member = memberRepository.findById(dto.getUserId())
                        .orElseThrow(() -> new CustomException(ErrorCode.ID_NOT_FOUND));

                List<ModelRecommendationResult> results = Optional.ofNullable(dto.getResult())
                        .orElseGet(ArrayList::new);

                if (!results.isEmpty()) {
                    log.info("[Service saveInternal] memberId={} 에 대한 새 추천 결과 {}개 발견. 기존 데이터 삭제 후 저장 시작.", member.getId(), results.size());
                    recommendationRepository.deleteByMemberIdAndDate(member.getId(), LocalDate.now());

                    for (ModelRecommendationResult r : results) {
                        ClothingRecommendation entity = convertToEntityWithBuilder(r, member.getId(), newRegionName);
                        recommendationRepository.save(entity);
                    }
                    log.info("[추천 데이터 저장 완료] memberId={}", member.getId());
                    recommendationRepository.flush();
                    entityManager.clear();
                }
                else {
                    log.info("[Service saveInternal] memberId={} 에 대한 AI 결과 없음. 기존 데이터 지역명 업데이트 시도.", member.getId());
                    List<ClothingRecommendation> existingData =
                            recommendationRepository.findByMemberIdAndDate(member.getId(), LocalDate.now());

                    if (!existingData.isEmpty()) {
                        log.info("[Service saveInternal] memberId={} 기존 데이터 {}개 발견. 지역명 업데이트 시작.", member.getId(), existingData.size());
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
            log.info("[Service saveInternal END] region={}", newRegionName);

        } catch (CustomException e) {
            log.warn("[Service saveInternal WARN] region={}, CustomException: {}", newRegionName, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[Service saveInternal ERROR] region={}, Unexpected Exception: {}", newRegionName, e.getMessage(), e);
            log.error("[Service saveInternal ERROR] Failed to parse JSON (first 200 chars): {}", decryptedJson != null && decryptedJson.length() > 200 ? decryptedJson.substring(0, 200) : decryptedJson);
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
}
