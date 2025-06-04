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
        Set<String> seen = new HashSet<>();
        List<int[]> result = new ArrayList<>();

        for (Map<Long, Integer> map : uppersList) {
            List<Integer> values = new ArrayList<>(map.values());

            if (values.size() >= 1) {
                int[] oneElem = new int[] { values.get(0) };
                String key = Arrays.toString(oneElem);
                if (!seen.contains(key)) {
                    seen.add(key);
                    result.add(oneElem);
                }
            }

            if (values.size() >= 2) {
                int[] twoElem = new int[] { values.get(0), values.get(1) };
                Arrays.sort(twoElem);
                String key = Arrays.toString(twoElem);
                if (!seen.contains(key)) {
                    seen.add(key);
                    result.add(twoElem);
                }
            }
        }
        return result;
    }

    private List<Map<Long, Integer>> getAllOutersList(List<Outer> outerList) {
        List<Map<Long, Integer>> ans = new ArrayList<>();
        for (Outer outer : outerList) {
            Long type = outer.getOuterType();
            int thickness = outer.getThickness();
            if (thickness == 1) {
                Optional<Integer> value = clothRepository.findThinByCategoryAndClothType(2, type.intValue());
                value.ifPresent(integer -> ans.add(Map.of(type, integer)));
                if (value.isEmpty()) throw new CustomException(ErrorCode.CLOTH_NOT_FOUND);
            }
            else if (thickness == 2) {
                Optional<Integer> value = clothRepository.findNormalByCategoryAndClothType(2, type.intValue());
                value.ifPresent(integer -> ans.add(Map.of(type, integer)));
                if (value.isEmpty()) throw new CustomException(ErrorCode.CLOTH_NOT_FOUND);
            }

            else if (thickness == 3) {
                Optional<Integer> value = clothRepository.findThickByCategoryAndClothType(2, type.intValue());
                value.ifPresent(integer -> ans.add(Map.of(type, integer)));
                if (value.isEmpty()) throw new CustomException(ErrorCode.CLOTH_NOT_FOUND);
            }
        }
        return ans;
    }

    private List<Map<Long, Integer>> getAllUppersList(List<Upper> upperList) {
        List<Map<Long, Integer>> ans = new ArrayList<>();
        for (Upper upper : upperList) {
            Long type = upper.getUpperType();
            int thickness = upper.getThickness();
            if (thickness == 1) {
                Optional<Integer> value = clothRepository.findThinByCategoryAndClothType(1, type.intValue());
                value.ifPresent(integer -> ans.add(Map.of(type, integer)));
                if (value.isEmpty()) throw new CustomException(ErrorCode.CLOTH_NOT_FOUND);
            }
            else if (thickness == 2) {
                Optional<Integer> value = clothRepository.findNormalByCategoryAndClothType(1, type.intValue());
                value.ifPresent(integer -> ans.add(Map.of(type, integer)));
                if (value.isEmpty()) throw new CustomException(ErrorCode.CLOTH_NOT_FOUND);
            }

            else if (thickness == 3) {
                Optional<Integer> value = clothRepository.findThickByCategoryAndClothType(1, type.intValue());
                value.ifPresent(integer -> ans.add(Map.of(type, integer)));
                if (value.isEmpty()) throw new CustomException(ErrorCode.CLOTH_NOT_FOUND);
            }
        }
        return ans;
    }
}
