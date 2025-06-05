package com.howWeather.howWeather_backend.domain.ai_model.service;

import com.howWeather.howWeather_backend.domain.ai_model.dto.AiPredictionRequestDto;
import com.howWeather.howWeather_backend.domain.ai_model.dto.ClothingCombinationDto;
import com.howWeather.howWeather_backend.domain.ai_model.dto.WeatherPredictDto;
import com.howWeather.howWeather_backend.domain.closet.entity.Outer;
import com.howWeather.howWeather_backend.domain.closet.entity.Upper;
import com.howWeather.howWeather_backend.domain.closet.repository.ClothRepository;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.weather.entity.WeatherForecast;
import com.howWeather.howWeather_backend.domain.weather.repository.WeatherForecastRepository;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AiInternalService {
    private final ClothRepository clothRepository;
    private final WeatherForecastRepository weatherForecastRepository;

    public AiPredictionRequestDto makePredictRequest(Member member) {
        List<WeatherPredictDto> dtoList = getWeatherForecast();
        return AiPredictionRequestDto.builder()
                .userId(String.valueOf(member.getId()))
                .bodyTypeLabel(member.getConstitution())
                .weatherForecast(dtoList)
                .clothingCombinations(fetchClothingCombinationsForUser(member))
                .build();
    }

    private List<WeatherPredictDto> getWeatherForecast() {
        List<WeatherForecast> forecasts = weatherForecastRepository
                .findByRegionNameAndForecastDate("서울특별시 용산구", LocalDate.now());

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

    private List<ClothingCombinationDto> fetchClothingCombinationsForUser(Member member) {
        List<int[]> uppers = getAllUppersCombi(member.getCloset().getUpperList());
        List<int[]> outers = getAllOutersCombi(member.getCloset().getOuterList());

        List<ClothingCombinationDto> combinations = new ArrayList<>();

        for (int[] upper : uppers) {
            for (int[] outer : outers) {
                ClothingCombinationDto dto = ClothingCombinationDto.builder()
                        .top(Arrays.stream(upper).boxed().collect(Collectors.toList()))
                        .outer(Arrays.stream(outer).boxed().collect(Collectors.toList()))
                        .build();
                combinations.add(dto);
            }
        }

        for (int[] upper : uppers) {
            ClothingCombinationDto dto = ClothingCombinationDto.builder()
                    .top(Arrays.stream(upper).boxed().collect(Collectors.toList()))
                    .outer(new ArrayList<>())
                    .build();
            combinations.add(dto);
        }

        return combinations;
    }


    private List<int[]> getAllOutersCombi(List<Outer> outerList) {
        return makeOneElementArrays(getAllOutersList(outerList));
    }


    private List<int[]> getAllUppersCombi(List<Upper> upperList) {
        return makeOneOrTwoElementArrays(getAllUppersList(upperList));
    }

    private List<int[]> makeOneElementArrays(List<Map<Long, Integer>> outersList) {
        List<int[]> result = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();

        for (Map<Long, Integer> map : outersList) {
            for (Integer value : map.values()) {
                if (!seen.contains(value)) {
                    seen.add(value);
                    result.add(new int[] { value });
                }
            }
        }
        return result;
    }

    private List<int[]> makeOneOrTwoElementArrays(List<Map<Long, Integer>> uppersList) {
        Set<Integer> allValues = uppersList.stream()
                .flatMap(map -> map.values().stream())
                .collect(Collectors.toSet());

        List<int[]> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (Integer val : allValues) {
            int[] one = new int[] { val };
            String key = Arrays.toString(one);
            if (seen.add(key)) {
                result.add(one);
            }
        }

        List<Integer> valueList = new ArrayList<>(allValues);
        int n = valueList.size();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int[] two = new int[] { valueList.get(i), valueList.get(j) };
                Arrays.sort(two);
                String key = Arrays.toString(two);
                if (seen.add(key)) {
                    result.add(two);
                }
            }
        }

        return result;
    }


    private List<Map<Long, Integer>> getAllOutersList(List<Outer> outerList) {
        Set<Long> seenTypes = new HashSet<>(); 
        List<Map<Long, Integer>> ans = new ArrayList<>();

        for (Outer outer : outerList) {
            Long type = outer.getOuterType();
            if (seenTypes.contains(type)) continue;

            int thickness = outer.getThickness();
            Optional<Integer> value = Optional.empty();

            if (thickness == 1) {
                value = clothRepository.findThinByCategoryAndClothType(2, type.intValue());
            } else if (thickness == 2) {
                value = clothRepository.findNormalByCategoryAndClothType(2, type.intValue());
            } else if (thickness == 3) {
                value = clothRepository.findThickByCategoryAndClothType(2, type.intValue());
            }

            if (value.isPresent()) {
                ans.add(Map.of(type, value.get()));
                seenTypes.add(type);
            } else {
                throw new CustomException(ErrorCode.CLOTH_NOT_FOUND);
            }
        }

        return ans;
    }


    private List<Map<Long, Integer>> getAllUppersList(List<Upper> upperList) {
        Set<Long> seenTypes = new HashSet<>(); // type만으로 중복 체크
        List<Map<Long, Integer>> ans = new ArrayList<>();

        for (Upper upper : upperList) {
            Long type = upper.getUpperType();
            if (seenTypes.contains(type)) continue; // 중복 type 제거

            int thickness = upper.getThickness();
            Optional<Integer> value = Optional.empty();

            if (thickness == 1) {
                value = clothRepository.findThinByCategoryAndClothType(1, type.intValue());
            } else if (thickness == 2) {
                value = clothRepository.findNormalByCategoryAndClothType(1, type.intValue());
            } else if (thickness == 3) {
                value = clothRepository.findThickByCategoryAndClothType(1, type.intValue());
            }

            if (value.isPresent()) {
                ans.add(Map.of(type, value.get()));
                seenTypes.add(type);
            } else {
                throw new CustomException(ErrorCode.CLOTH_NOT_FOUND);
            }
        }

        return ans;
    }

}
