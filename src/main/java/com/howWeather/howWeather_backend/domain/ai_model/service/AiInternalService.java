package com.howWeather.howWeather_backend.domain.ai_model.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.howWeather.howWeather_backend.domain.ai_model.dto.AiPredictionRequestDto;
import com.howWeather.howWeather_backend.domain.ai_model.dto.ClothingCombinationDto;
import com.howWeather.howWeather_backend.domain.ai_model.dto.WeatherPredictDto;
import com.howWeather.howWeather_backend.domain.ai_model.entity.ClothingCombination;
import com.howWeather.howWeather_backend.domain.ai_model.repository.ClothingCombinationRepository;
import com.howWeather.howWeather_backend.domain.closet.entity.Outer;
import com.howWeather.howWeather_backend.domain.closet.entity.Upper;
import com.howWeather.howWeather_backend.domain.closet.repository.ClothRepository;
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
    private final ClothRepository clothRepository;
    private final WeatherForecastRepository weatherForecastRepository;
    private final ClothingCombinationRepository combinationRepository;
    private final ClothingCombinationService combinationService;
    private final ObjectMapper objectMapper;

    public List<AiPredictionRequestDto> makePredictRequestsSafely(List<Member> members) {
        return members.stream()
                .filter(member -> member.getCloset() != null)
                .map(member -> {
                    try {
                        return makePredictRequest(member);
                    } catch (Exception e) {
                        log.error("멤버 {} 예측 데이터 생성 실패: {}", member.getId(), e.getMessage());
                        return null; // 실패 시 null 반환
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Transactional
    public AiPredictionRequestDto makePredictRequest(Member member) {
        List<WeatherPredictDto> weather = getWeatherForecast(member);

        List<ClothingCombinationDto> combinations;
        try {
            combinations = combinationService.getOrCalculateCombinations(member);
        } catch (JsonProcessingException e) {
            log.error("의상 조합 가져오기 실패: {}", e.getMessage(), e);
            combinations = Collections.emptyList();
        }

        return AiPredictionRequestDto.builder()
                .userId(member.getId())
                .bodyTypeLabel(member.getConstitution())
                .weatherForecast(weather)
                .clothingCombinations(combinations)
                .build();
    }

    private void saveCombinations(Long memberId, List<ClothingCombinationDto> combinations) {
        try {
            String json = objectMapper.writeValueAsString(combinations);
            ClothingCombination entity = combinationRepository.findByMemberId(memberId)
                    .orElse(ClothingCombination.builder().memberId(memberId).build());

            entity.updateCombinations(json, LocalDate.now());
            combinationRepository.save(entity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("조합 JSON 변환 실패", e);
        }
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

    public List<ClothingCombinationDto> fetchClothingCombinationsForUser(Member member) {
        List<Map<Long, Integer>> uppers = getAllUppersList(member.getCloset().getUpperList());
        List<Map<Long, Integer>> outers = getAllOutersList(member.getCloset().getOuterList());

        return fetchClothingCombinationsForUserStream(uppers, outers).distinct().collect(Collectors.toList());
    }

    private Stream<ClothingCombinationDto> fetchClothingCombinationsForUserStream(
            List<Map<Long, Integer>> uppers, List<Map<Long, Integer>> outers) {

        Stream<ClothingCombinationDto> upperOne = generateUpperOneCombinations(uppers, outers);
        Stream<ClothingCombinationDto> upperTwo = generateUpperTwoCombinations(uppers, outers);

        return Stream.concat(upperOne, upperTwo);
    }

    private Stream<ClothingCombinationDto> generateUpperOneCombinations(
            List<Map<Long, Integer>> uppers, List<Map<Long, Integer>> outers) {

        return uppers.stream()
                .flatMap(u -> {
                    Integer upperType = u.keySet().iterator().next().intValue();
                    ClothingCombinationDto withoutOuter = createCombination(upperType, null); // 아우터 0벌
                    Stream<ClothingCombinationDto> withOuter = outers.stream() // 아우터 1벌
                            .map(o -> {
                                Integer outerType = o.keySet().iterator().next().intValue();
                                return createCombination(upperType, outerType);
                            });

                    return Stream.concat(Stream.of(withoutOuter), withOuter);
                });
    }

    private Stream<ClothingCombinationDto> generateUpperTwoCombinations(
            List<Map<Long, Integer>> uppers, List<Map<Long, Integer>> outers) {

        return IntStream.range(0, uppers.size())
                .boxed()
                .flatMap(i -> IntStream.range(i + 1, uppers.size())
                        .mapToObj(j -> {
                            Map<Long, Integer> u1 = uppers.get(i);
                            Map<Long, Integer> u2 = uppers.get(j);

                            Integer type1 = u1.keySet().iterator().next().intValue();
                            Integer type2 = u2.keySet().iterator().next().intValue();

                            if (type1.equals(type2)) return null;

                            ClothingCombinationDto withoutOuter = createCombination(type1, type2, null);

                            Stream<ClothingCombinationDto> withOuter = outers.stream()
                                    .map(o -> {
                                        Integer outerType = o.keySet().iterator().next().intValue();
                                        return createCombination(type1, type2, outerType);
                                    });

                            return Stream.concat(Stream.of(withoutOuter), withOuter);
                        })
                        .filter(Objects::nonNull)
                        .flatMap(s -> s)
                );
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

    private List<Map<Long, Integer>> getAllOutersList(List<Outer> outerList) {
        List<Map<Long, Integer>> ans = new ArrayList<>();

        for (Outer outer : outerList) {
            ans.add(Map.of(outer.getOuterType(), outer.getWarmthIndex()));
        }

        return ans;
    }

    private List<Map<Long, Integer>> getAllUppersList(List<Upper> upperList) {
        List<Map<Long, Integer>> ans = new ArrayList<>();

        for (Upper upper : upperList) {
            ans.add(Map.of(upper.getUpperType(), upper.getWarmthIndex()));
        }

        return ans;
    }
}