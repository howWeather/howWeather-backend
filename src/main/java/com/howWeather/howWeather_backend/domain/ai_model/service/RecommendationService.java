package com.howWeather.howWeather_backend.domain.ai_model.service;

import com.howWeather.howWeather_backend.domain.ai_model.dto.ModelClothingRecommendationDto;
import com.howWeather.howWeather_backend.domain.ai_model.dto.ModelRecommendationResult;
import com.howWeather.howWeather_backend.domain.ai_model.dto.RecommendPredictDto;
import com.howWeather.howWeather_backend.domain.ai_model.dto.WeatherFeelingDto;
import com.howWeather.howWeather_backend.domain.ai_model.entity.ClothingRecommendation;
import com.howWeather.howWeather_backend.domain.ai_model.repository.ClothingRecommendationRepository;
import com.howWeather.howWeather_backend.domain.closet.entity.Closet;
import com.howWeather.howWeather_backend.domain.closet.entity.Outer;
import com.howWeather.howWeather_backend.domain.closet.entity.Upper;
import com.howWeather.howWeather_backend.domain.closet.repository.ClosetRepository;
import com.howWeather.howWeather_backend.domain.closet.repository.ClothRepository;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.weather.entity.WeatherForecast;
import com.howWeather.howWeather_backend.domain.weather.repository.WeatherForecastRepository;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class RecommendationService {

    private final ClothingRecommendationRepository clothingRecommendationRepository;
    private final ClothRepository clothRepository;
    private final WeatherForecastRepository weatherForecastRepository;
    private final ClosetRepository closetRepository;

    @Transactional(readOnly = true)
    public List<RecommendPredictDto> getRecommendList(Member member) {
        List<ClothingRecommendation> modelPredictList = getModelPrediction(member.getId(), LocalDate.now());

        Closet closet = getClosetWithAll(member);
        List<RecommendPredictDto> result = new ArrayList<>();
        for (ClothingRecommendation recommendation : modelPredictList) {
            result.add(makeResultForPredict(closet, recommendation, member));
        }
        return result;
    }

    private RecommendPredictDto makeResultForPredict(Closet closet, ClothingRecommendation recommendation, Member member) {
        try {
            List<Integer> upperTypeList = makeUpperList(closet, recommendation.getTops());
            List<Integer> outerTypeList = makeOuterList(closet, recommendation.getOuters());
            List<WeatherFeelingDto> weatherFeelingDto = makeWeatherFeeling(recommendation.getPredictionMap(), member);

            return RecommendPredictDto.builder()
                    .feelingList(weatherFeelingDto)
                    .outersTypeList(outerTypeList)
                    .uppersTypeList(upperTypeList)
                    .build();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "예측 데이터를 변환하는 중 오류가 발생하였습니다: " + e.getMessage());
        }
    }
    
    // TODO : AI 학습 이후 사용자가 대량으로 의상을 삭제해서 추천할 의상이 없어져버리는 경우에 대한 고민이 필요해보임.

    private List<Integer> makeOuterList(Closet closet, List<Integer> outers) {
        if (outers.isEmpty()) return new ArrayList<>();

        Set<Long> ownedClothTypes = closet.getOuterList().stream()
                .map(Outer::getOuterType)
                .collect(Collectors.toSet());

        Set<Integer> resultSet = new HashSet<>();
        for (Integer outerHeat : outers) {
            List<Long> candidateTypes = clothRepository.findClothTypeByCategoryAndHeat(2, outerHeat);
            for (Long type : candidateTypes) {
                if (ownedClothTypes.contains(type)) {
                    resultSet.add(type.intValue());
                }
            }
        }

        return new ArrayList<>(resultSet);
    }

    private List<Integer> makeUpperList(Closet closet, List<Integer> tops) {
        Set<Long> ownedClothTypes = closet.getUpperList().stream()
                .map(Upper::getUpperType)
                .collect(Collectors.toSet());

        Set<Integer> resultSet = new HashSet<>();
        for (Integer topHeat : tops) {
            List<Long> candidateTypes = clothRepository.findClothTypeByCategoryAndHeat(1, topHeat);
            for (Long type : candidateTypes) {
                if (ownedClothTypes.contains(type)) {
                    resultSet.add(type.intValue());
                }
            }
        }
        return new ArrayList<>(resultSet);
    }

    private List<WeatherFeelingDto> makeWeatherFeeling(Map<String, Integer> predictionMap, Member member) {
        List<WeatherFeelingDto> feelingList = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalDate forecastDate = now.isBefore(LocalTime.of(6, 0)) ? today.minusDays(1) : today;

        String regionName = (member.getRegionName() != null) ? member.getRegionName() : "서울특별시 용산구";

        List<Integer> hours = predictionMap.keySet().stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        List<WeatherForecast> forecasts = weatherForecastRepository
                .findByRegionNameAndForecastDateAndHourIn(regionName, forecastDate, hours);

        Map<Integer, WeatherForecast> hourToForecastMap = forecasts.stream()
                .collect(Collectors.toMap(
                        WeatherForecast::getHour,
                        forecast -> forecast,
                        (oldVal, newVal) -> newVal
                ));

        for (Map.Entry<String, Integer> entry : predictionMap.entrySet()) {
            int hour = Integer.parseInt(entry.getKey());
            int feeling = entry.getValue();
            WeatherForecast forecast = hourToForecastMap.get(hour);

            if (forecast != null) {
                WeatherFeelingDto dto = WeatherFeelingDto.builder()
                        .date(forecast.getForecastDate())
                        .time(hour)
                        .feeling(feeling)
                        .temperature(forecast.getTemperature())
                        .build();
                feelingList.add(dto);
            } else {
                throw new CustomException(ErrorCode.WEATHER_DATA_NOT_FOUND);
            }
        }

        return feelingList;
    }

    private List<ClothingRecommendation> getModelPrediction(Long id, LocalDate now) {
        List<ClothingRecommendation> list = clothingRecommendationRepository.findByMemberIdAndDate(id, now);
        if (list.isEmpty()) {
            throw new CustomException(ErrorCode.NO_PREDICT_DATA);
        }
        return list;
    }

    @Transactional
    public void save(ModelClothingRecommendationDto dto) {
        try {
            for (ModelRecommendationResult result : dto.getResult()) {
                ClothingRecommendation recommendation = ClothingRecommendation.builder()
                        .memberId(dto.getUserId())
                        .tops(result.getTops())
                        .outers(result.getOuters())
                        .predictionMap(result.getPredictFeeling())
                        .date(LocalDate.now())
                        .build();

                clothingRecommendationRepository.save(recommendation);
            }
        } catch (Exception e) {
            log.error("모델로부터 예측 결과 저장 중 오류", e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "예측 결과 저장 중 오류가 발생했습니다.");
        }
    }
    private Closet getClosetWithAll(Member member) {
        Closet closetWithUppers = closetRepository.findClosetWithUppers(member.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.CLOSET_NOT_FOUND));

        return closetRepository.findClosetWithOuters(closetWithUppers.getId())
                .orElse(closetWithUppers);
    }
}