package com.howWeather.howWeather_backend.domain.ai_model.service;

import com.howWeather.howWeather_backend.domain.ai_model.dto.AiPredictionRequestDto;
import com.howWeather.howWeather_backend.domain.ai_model.dto.ClothingCombinationDto;
import com.howWeather.howWeather_backend.domain.ai_model.dto.WeatherPredictDto;
import com.howWeather.howWeather_backend.domain.closet.entity.Outer;
import com.howWeather.howWeather_backend.domain.closet.entity.Upper;
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
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

        List<WeatherForecast> forecasts = weatherForecastRepository
                .findByRegionNameAndForecastDateAndHourIn(regionName, LocalDate.now(), targetHours);

        return forecasts.stream()
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

    private ClothingCombinationDto createCombination(Integer upperType, Integer outerType) {
        List<Integer> top = List.of(upperType);
        List<Integer> outerList = outerType != null ? List.of(outerType) : new ArrayList<>();
        return ClothingCombinationDto.builder()
                .top(top)
                .outer(outerList)
                .build();
    }

    private ClothingCombinationDto createCombination(Integer upperType1, Integer upperType2, Integer outerType) {
        List<Integer> top = List.of(upperType1, upperType2);
        List<Integer> outerList = outerType != null ? List.of(outerType) : new ArrayList<>();
        return ClothingCombinationDto.builder()
                .top(top)
                .outer(outerList)
                .build();
    }

    private List<Map<Long, Integer>> getAllUppersList(List<Upper> upperList) {
        return upperList.stream()
                .filter(Upper::isActive)
                .map(u -> Map.of(u.getUpperType(), u.getWarmthIndex()))
                .collect(Collectors.toList());
    }

    private List<Map<Long, Integer>> getAllOutersList(List<Outer> outerList) {
        return outerList.stream()
                .filter(Outer::isActive)
                .map(o -> Map.of(o.getOuterType(), o.getWarmthIndex()))
                .collect(Collectors.toList());
    }
}