package com.howWeather.howWeather_backend.domain.location.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.howWeather.howWeather_backend.domain.location.dto.RegionInfoDto;
import com.howWeather.howWeather_backend.domain.weather.entity.Weather;
import com.howWeather.howWeather_backend.domain.location.dto.RegionTemperatureDto;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    private final WeatherRepository weatherRepository;

    public RegionInfoDto reverseGeocode(double latitude, double longitude) {
        try {
            String url = "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json?x=" + longitude + "&y=" + latitude;

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

            JsonNode documents = response.getBody().get("documents");
            if (documents.isArray() && !documents.isEmpty()) {
                JsonNode region = documents.get(0);

                RegionInfoDto dto = new RegionInfoDto();
                dto.setAddress_name(region.get("address_name").asText());
                dto.setRegion_1depth_name(region.get("region_1depth_name").asText());
                dto.setRegion_2depth_name(region.get("region_2depth_name").asText());
                return dto;
            } else {
                throw new  CustomException(ErrorCode. KAKAO_API_REGION_NOT_FOUND);
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("카카오 위치 찾기 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "카카오 위치 찾기 실패");
        }
    }

    @Transactional
    public RegionTemperatureDto getRegionTemperatureFromCoords(double latitude, double longitude, int timeSlot, LocalDate date) {
        try {
            RegionInfoDto regionInfo = reverseGeocode(latitude, longitude);
            String regionName = regionInfo.getRegion_2depth_name();

            if (date == null) {
                date = LocalDate.now();
            }

            Weather weather = weatherRepository.findByRegionNameAndDateAndTimeSlot(regionName, date, timeSlot)
                    .orElseThrow(() -> new CustomException(ErrorCode.REGION_NOT_FOUND));
            return new RegionTemperatureDto(regionInfo, weather.getTemperature());
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("카카오 위치 찾기 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "카카오 위치 찾기 실패");
        }
    }
}

