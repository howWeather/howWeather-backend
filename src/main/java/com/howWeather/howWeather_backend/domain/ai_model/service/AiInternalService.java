package com.howWeather.howWeather_backend.domain.ai_model.service;

import com.howWeather.howWeather_backend.domain.ai_model.dto.AiPredictionRequestDto;
import com.howWeather.howWeather_backend.domain.ai_model.dto.ClothingCombinationDto;
import com.howWeather.howWeather_backend.domain.ai_model.dto.WeatherPredictDto;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.weather.entity.WeatherForecast;
import com.howWeather.howWeather_backend.domain.weather.repository.WeatherForecastRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
@AllArgsConstructor
@Slf4j
public class AiInternalService {
    private final WeatherForecastRepository weatherForecastRepository;
    private final ClothingCombinationService combinationService;

    public List<AiPredictionRequestDto> makePredictRequestsSafely(List<Member> members) {
        return members.stream()
                .filter(member -> member.getCloset() != null)
                .map(member -> {
                    try {
                        return makePredictRequest(member);
                    } catch (Exception e) {
                        log.error("멤버 {} 예측 데이터 생성 실패: {}", member.getId(), e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Transactional
    public AiPredictionRequestDto makePredictRequest(Member member) {
        if (member.getCloset().isNeedsCombinationRefresh()) {
            combinationService.refreshDailyCombinations(member);
            member.getCloset().updateFinish();
        }

        List<WeatherPredictDto> weather = getWeatherForecast(member);
        List<ClothingCombinationDto> combinations = combinationService.fetchPrecomputedCombinations(member);

        return AiPredictionRequestDto.builder()
                .userId(member.getId())
                .bodyTypeLabel(member.getConstitution())
                .weatherForecast(weather)
                .clothingCombinations(combinations)
                .build();
    }

    private List<WeatherPredictDto> getWeatherForecast(Member member) {
        List<Integer> targetHours = List.of(9, 12, 15, 18, 21);
        String regionName = member.getRegionName() != null ? member.getRegionName() : "서울특별시 용산구";

        LocalDate today = LocalDate.now();

        List<WeatherForecast> forecasts = weatherForecastRepository
                .findByRegionNameAndForecastDateAndHourInOrderByCreatedAtDesc(regionName, today, targetHours);

        if (forecasts.isEmpty()) {
            LocalDate yesterday = today.minusDays(1);
            log.warn("[날씨 조회 재시도] 지역 {}에 대해 오늘({}) 데이터가 없어 어제({}) 날짜로 재시도합니다.",
                    regionName, today, yesterday);

            forecasts = weatherForecastRepository
                    .findByRegionNameAndForecastDateAndHourInOrderByCreatedAtDesc(regionName, yesterday, targetHours);

            if (forecasts.isEmpty()) {
                log.error("[날씨 조회 최종 실패] 지역 {}에 대해 어제, 오늘 모두 예보 데이터가 없습니다.", regionName);
            }
        }

        log.info("[날씨 조회 결과] memberId={}, 지역: {}, 조회된 예보 개수: {}",
                member.getId(), regionName, forecasts.size());

        Map<Integer, WeatherForecast> hourToForecastMap = forecasts.stream()
                .collect(Collectors.toMap(
                        WeatherForecast::getHour,
                        forecast -> forecast,
                        (oldVal, newVal) -> oldVal
                ));

        return hourToForecastMap.values().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    private WeatherPredictDto convertToDto(WeatherForecast forecast) {
        return WeatherPredictDto.builder()
                .hour(forecast.getHour())
                .windSpeed(forecast.getWindSpeed())
                .cloudAmount(forecast.getCloudAmount())
                .temperature(forecast.getTemperature())
                .humidity(forecast.getHumidity())
                .precipitation(forecast.getPrecipitation())
                .feelsLike(forecast.getFeelsLike())
                .build();
    }
}