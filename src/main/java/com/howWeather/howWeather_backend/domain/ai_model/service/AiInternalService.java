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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AiInternalService {
    private final ClothRepository clothRepository;
    private final WeatherForecastRepository weatherForecastRepository;

    @Transactional
    public AiPredictionRequestDto makePredictRequest(Member member) {
        List<WeatherPredictDto> dtoList = getWeatherForecast();
        return AiPredictionRequestDto.builder()
                .userId(member.getId())
                .bodyTypeLabel(member.getConstitution())
                .weatherForecast(dtoList)
                .clothingCombinations(fetchClothingCombinationsForUser(member))
                .build();
    }

    private List<WeatherPredictDto> getWeatherForecast() {
        List<Integer> targetHours = List.of(9, 12, 15, 18, 21);

        List<WeatherForecast> forecasts = weatherForecastRepository
                .findByRegionNameAndForecastDateAndHourIn("서울특별시 용산구", LocalDate.now(), targetHours);

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

        Set<ClothingCombinationDto> unique = new LinkedHashSet<>(combinations);
        return new ArrayList<>(unique);
    }


    private List<int[]> getAllOutersCombi(List<Outer> outerList) {
        return makeOneElementArrays(getAllOutersList(outerList));
    }


    private List<int[]> getAllUppersCombi(List<Upper> upperList) {
        return makeOneOrTwoElementArrays(getAllUppersList(upperList));
    }

    private List<int[]> makeOneElementArrays(List<Map<Long, Integer>> clothList) {
        List<int[]> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (Map<Long, Integer> map : clothList) {
            for (Map.Entry<Long, Integer> entry : map.entrySet()) {
                String key = entry.getKey() + ":" + entry.getValue();
                if (!seen.contains(key)) {
                    seen.add(key);
                    result.add(new int[] { entry.getValue() });
                }
            }
        }
        return result;
    }

    private List<int[]> makeOneOrTwoElementArrays(List<Map<Long, Integer>> clothList) {
        List<int[]> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (Map<Long, Integer> map : clothList) {
            for (Map.Entry<Long, Integer> entry : map.entrySet()) {
                String key = entry.getKey() + ":" + entry.getValue();
                if (seen.add(key)) {
                    result.add(new int[] { entry.getValue() });
                }
            }
        }

        List<Map.Entry<Long, Integer>> entries = clothList.stream()
                .flatMap(m -> m.entrySet().stream())
                .toList();

        int n = entries.size();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int[] pair = new int[] { entries.get(i).getValue(), entries.get(j).getValue() };
                Arrays.sort(pair);

                String key = entries.get(i).getKey() + ":" + entries.get(i).getValue() + "," +
                        entries.get(j).getKey() + ":" + entries.get(j).getValue();

                if (seen.add(key)) {
                    result.add(pair);
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
            } else {
                throw new CustomException(ErrorCode.CLOTH_NOT_FOUND);
            }
        }

        return ans;
    }

    private List<Map<Long, Integer>> getAllUppersList(List<Upper> upperList) {
        List<Map<Long, Integer>> ans = new ArrayList<>();

        for (Upper upper : upperList) {
            Long type = upper.getUpperType();

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
            } else {
                throw new CustomException(ErrorCode.CLOTH_NOT_FOUND);
            }
        }

        return ans;
    }

}
