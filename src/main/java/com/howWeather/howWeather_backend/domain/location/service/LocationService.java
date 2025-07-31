package com.howWeather.howWeather_backend.domain.location.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.howWeather.howWeather_backend.domain.location.dto.RegionInfoDto;
import com.howWeather.howWeather_backend.domain.weather.entity.Region;
import com.howWeather.howWeather_backend.domain.weather.entity.Weather;
import com.howWeather.howWeather_backend.domain.location.dto.RegionTemperatureDto;
import com.howWeather.howWeather_backend.domain.weather.repository.RegionRepository;
import com.howWeather.howWeather_backend.domain.weather.repository.WeatherRepository;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    private final WeatherRepository weatherRepository;
    private final RegionRepository regionRepository;

    public String reverseGeocode(double latitude, double longitude) {
        try {
            boolean latitudeValidation = validateData("latitude", latitude, -90, 90);
            boolean longitudeValidation = validateData("longitude", longitude, -180, 180);

            if (!latitudeValidation  || !longitudeValidation) {
                throw new CustomException(ErrorCode.INVALID_INPUT, "위/경도 값이 올바르지 않습니다.");
            }
            String url = "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json?x=" + longitude + "&y=" + latitude;

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

            JsonNode documents = response.getBody().get("documents");
            if (documents.isArray() && !documents.isEmpty()) {
                JsonNode region = documents.get(0);

                String regionName = region.get("region_1depth_name").asText() + " " + region.get("region_2depth_name").asText();
                return regionName;
            } else {
                throw new CustomException(ErrorCode.KAKAO_API_REGION_NOT_FOUND);
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("카카오 위치 찾기 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "카카오 위치 찾기 실패");
        }
    }

    private boolean validateData(String name, double data, int low, int high) {
        if (data < low || data > high) {
            return false;
        }
        return true;
    }

    @Transactional
    public RegionTemperatureDto getRegionTemperatureFromCoords(
            double latitude, double longitude, int timeSlot, LocalDate date) {
        try {
            String fullRegion = reverseGeocode(latitude, longitude);
            String matchedRegionName = resolveRegionName(fullRegion);

            if (date == null) {
                date = LocalDate.now();
            }
            validateRecordTime(date, timeSlot);

            Weather weather = weatherRepository.findByRegionNameAndDateAndTimeSlot(
                    matchedRegionName, date, timeSlot
            ).orElseThrow(() -> new CustomException(ErrorCode.REGION_NOT_FOUND));

            return new RegionTemperatureDto(matchedRegionName, weather.getTemperature());

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("위치 처리 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "위치 처리 실패");
        }
    }

    private void validateRecordTime(LocalDate date, int timeSlot) {
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        LocalDate today = now.toLocalDate();
        LocalDate yesterday = today.minusDays(1);

        if (timeSlot < 1 || timeSlot > 3) {
            throw new CustomException(ErrorCode.INVALID_TIMESLOT, "유효하지 않은 시간대 기록 요청입니다.");
        }

        if (date.equals(today)) {
            LocalDateTime availableTime = getAvailableTime(date, timeSlot);
            if (now.toLocalDateTime().isBefore(availableTime)) {
                throw new CustomException(ErrorCode.UNABLE_RECORD_TIME, "아직 해당 시간대(" + timeSlotToLabel(timeSlot) + ") 기록을 작성할 수 없습니다.");
            }
            return;
        }

        if (date.equals(yesterday)) {
            ZonedDateTime deadline = today.atTime(5, 30).atZone(zoneId);
            if (now.isAfter(deadline)) {
                throw new CustomException(ErrorCode.TOO_LATE_TO_RECORD, "전날 기록은 오늘 새벽 5시 30분 이전까지만 작성할 수 있습니다.");
            }
            return;
        }

        throw new CustomException(ErrorCode.INVALID_DATE, "오늘 또는 어제 날짜에 대해서만 기록을 작성할 수 있습니다.");
    }

    private LocalDateTime getAvailableTime(LocalDate date, int timeSlot) {
        return switch (timeSlot) {
            case 1 -> date.atTime(9, 0);
            case 2 -> date.atTime(14, 0);
            case 3 -> date.atTime(20, 0);
            default -> throw new CustomException(ErrorCode.INVALID_TIMESLOT, "유효하지 않은 시간대 기록 요청입니다.");
        };
    }

    private String timeSlotToLabel(int timeSlot) {
        return switch (timeSlot) {
            case 1 -> "오전";
            case 2 -> "오후";
            case 3 -> "저녁";
            default -> "알 수 없음";
        };
    }

    public String resolveRegionName(String fullRegionFromKakao) {
        Optional<Region> exactMatch = regionRepository.findByName(fullRegionFromKakao);
        if (exactMatch.isPresent()) {
            return exactMatch.get().getName();
        }

        List<String> regionNames = regionRepository.findAllRegionNames();

        String bestMatch = null;
        int maxPrefixLength = 0;

        for (String regionName : regionNames) {
            int prefixLength = getCommonPrefixLength(fullRegionFromKakao, regionName);
            if (prefixLength > maxPrefixLength) {
                maxPrefixLength = prefixLength;
                bestMatch = regionName;
            }
        }

        if (bestMatch == null) {
            throw new CustomException(ErrorCode.REGION_NOT_FOUND);
        }

        return bestMatch;
    }

    private int getCommonPrefixLength(String a, String b) {
        int minLength = Math.min(a.length(), b.length());
        int i = 0;
        while (i < minLength && a.charAt(i) == b.charAt(i)) {
            i++;
        }
        return i;
    }
}

