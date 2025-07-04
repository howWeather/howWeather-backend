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

