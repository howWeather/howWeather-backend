package com.howWeather.howWeather_backend.domain.member.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.howWeather.howWeather_backend.domain.ai_model.dto.AiPredictionRequestDto;
import com.howWeather.howWeather_backend.domain.ai_model.dto.ModelClothingRecommendationDto;
import com.howWeather.howWeather_backend.domain.ai_model.dto.WeatherPredictDto;
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
import com.howWeather.howWeather_backend.domain.weather.repository.RegionRepository;
import com.howWeather.howWeather_backend.global.cipher.AESCipher;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyAccountService {
    private final MemberRepository memberRepository;
    private final ClothingRecommendationRepository recommendationRepository;
    private final RecommendationService recommendationService;
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
    public String getLoccation(Member member) {
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
                pushPredictionForSingleMember(persistedMember);
            }

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("지역 수정 중 에러 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "지역 수정 중 오류가 발생했습니다.");
        }
    }

    /**
     * 단일 회원에 대해 스케줄러 AI 예측 전송 로직 재사용
     */
    private void pushPredictionForSingleMember(Member member) {
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

            log.info("[AI 예측 DTO 확인] memberId={}, bodyType={}, weatherForecast={}, clothingCombinations={}",
                    member.getId(),
                    dto.getUserId(),
                    dto.getBodyTypeLabel(),
                    dto.getWeatherForecast(),
                    dto.getClothingCombinations());

            Map<String, String> encryptedData = encryptPredictionData(List.of(dto));
            if (encryptedData == null || encryptedData.isEmpty()) return;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(encryptedData, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(aiServerUrl, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[AI 예측 전송 완료] memberId={}, 응답={}", member.getId(), response.getStatusCode());
            } else {
                log.warn("[AI 서버 응답 실패] memberId={}, 상태코드={}, 본문={}",
                        member.getId(), response.getStatusCode(), response.getBody());
            }

            saveRecommendationsInternal(encryptedData);

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

    private void saveRecommendationsInternal(Map<String, String> encryptedData) {
        try {
            if (encryptedData == null || encryptedData.isEmpty()) return;

            String decryptedJson = aesCipher.decrypt(encryptedData);
            if (decryptedJson.startsWith("\"") && decryptedJson.endsWith("\"")) {
                decryptedJson = objectMapper.readValue(decryptedJson, String.class);
            }

            ModelClothingRecommendationDto[] dtoArray = objectMapper.readValue(
                    decryptedJson, ModelClothingRecommendationDto[].class);

            for (ModelClothingRecommendationDto dto : dtoArray) {
                Member member = memberRepository.findById(dto.getUserId())
                        .orElseThrow(() -> new CustomException(ErrorCode.ID_NOT_FOUND));

                recommendationRepository.deleteByMemberIdAndDate(member.getId(), LocalDate.now());
                recommendationService.save(dto, member);
                log.info("[추천 데이터 저장 완료] memberId={}", member.getId());
            }

        } catch (Exception e) {
            log.error("[추천 데이터 저장 실패] message={}", e.getMessage(), e);
        }
    }
}
