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
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        LocalDate targetDate = now.isBefore(LocalTime.of(6, 0)) ? today.minusDays(1) : today;

        List<ClothingRecommendation> modelPredictList = getModelPrediction(member.getId(), targetDate);
        List<RecommendPredictDto> result = new ArrayList<>();
        Closet closet = getClosetWithAll(member);
        if (!modelPredictList.isEmpty()) {
            log.info("[AI 추천] memberId={} AI 예측 결과 사용 ({}개)", member.getId(), modelPredictList.size());
            for (ClothingRecommendation recommendation : modelPredictList) {
                RecommendPredictDto dto = makeResultForPredict(closet, recommendation, member);
                boolean isUppersExist = dto.getUppersTypeList() != null && !dto.getUppersTypeList().isEmpty();
                boolean isFeelingExist = dto.getFeelingList() != null && !dto.getFeelingList().isEmpty();
                if (isUppersExist && isFeelingExist) {
                    result.add(dto);
                }
            }
        } else {
            result.addAll(generateFallbackRecommendation(member, targetDate, closet));
        }

        return result;
    }

    private List<RecommendPredictDto> generateFallbackRecommendation(Member member, LocalDate targetDate, Closet closet) {
        log.warn("[Fallback 추천] memberId={} AI 예측 결과 없음. 대체 추천 로직 시작.", member.getId());
        List<RecommendPredictDto> fallbackResult = new ArrayList<>();

        try {
            final Long MIN_OUTER_TYPE = 4L;
            final Long MAX_OUTER_TYPE = 17L;
            final Long MIN_UPPER_TYPE = 3L;
            final Long MAX_UPPER_TYPE = 9L;

            List<Integer> fallbackOuterTypes = closet.getOuterList().stream()
                    .filter(Outer::isActive)
                    .filter(outer -> outer.getOuterType() != null &&
                            outer.getOuterType() >= MIN_OUTER_TYPE &&
                            outer.getOuterType() <= MAX_OUTER_TYPE)
                    .map(outer -> outer.getOuterType().intValue())
                    .distinct()
                    .collect(Collectors.toList());

            List<Integer> fallbackUpperTypes = closet.getUpperList().stream()
                    .filter(Upper::isActive)
                    .filter(upper -> upper.getUpperType() != null &&
                            upper.getUpperType() >= MIN_UPPER_TYPE &&
                            upper.getUpperType() <= MAX_UPPER_TYPE)
                    .map(upper -> upper.getUpperType().intValue())
                    .distinct()
                    .collect(Collectors.toList());

            if (fallbackOuterTypes.isEmpty() && fallbackUpperTypes.isEmpty()) {
                log.info("[Fallback 추천] memberId={} 추천할 활성 상의 또는 아우터 없음 (기준: Upper {}-{}, Outer {}-{}). 예외 발생.",
                        member.getId(), MIN_UPPER_TYPE, MAX_UPPER_TYPE, MIN_OUTER_TYPE, MAX_OUTER_TYPE);
                throw new CustomException(ErrorCode.NO_PREDICT_DATA);
            }

            List<WeatherFeelingDto> fallbackFeelingList = createFallbackFeelingList(member, targetDate);

            RecommendPredictDto fallbackDto = RecommendPredictDto.builder()
                    .feelingList(fallbackFeelingList)
                    .uppersTypeList(fallbackUpperTypes)
                    .outersTypeList(fallbackOuterTypes)
                    .build();
            fallbackResult.add(fallbackDto);
            log.info("[Fallback 추천] memberId={} 최종 추천 결과: Uppers={}, Outers={}, FeelingsGenerated={}",
                    member.getId(), fallbackUpperTypes, fallbackOuterTypes, !fallbackFeelingList.isEmpty());

        } catch (CustomException e) {
            log.error("[Fallback 추천 실패] memberId={} 처리 중 Custom 오류: {}", member.getId(), e.getMessage());
            throw new CustomException(ErrorCode.NO_PREDICT_DATA);
        } catch (Exception e) {
            log.error("[Fallback 추천 실패] memberId={} 알 수 없는 오류 발생: {}", member.getId(), e.getMessage(), e);
            throw new CustomException(ErrorCode.NO_PREDICT_DATA);
        }

        return fallbackResult;
    }

    private List<WeatherFeelingDto> createFallbackFeelingList(Member member, LocalDate targetDate) {
        List<WeatherFeelingDto> fallbackFeelingList = new ArrayList<>();
        String regionName = member.getRegionName() != null ? member.getRegionName() : "서울특별시 용산구";
        List<Integer> targetHours = List.of(9, 12, 15, 18, 21);
        final int DEFAULT_FEELING = 2;

        try {
            List<WeatherForecast> forecasts = weatherForecastRepository
                    .findByRegionNameAndForecastDateAndHourInOrderByHourAsc(regionName, targetDate, targetHours);

            if (!forecasts.isEmpty()) {
                for (WeatherForecast forecast : forecasts) {
                    WeatherFeelingDto dto = WeatherFeelingDto.builder()
                            .date(forecast.getForecastDate())
                            .time(forecast.getHour())
                            .feeling(DEFAULT_FEELING)
                            .temperature(forecast.getTemperature())
                            .build();
                    fallbackFeelingList.add(dto);
                }
                log.info("[Fallback 추천] memberId={} 기본 체감온도(2)로 그래프 생성 완료 ({}개 시간대)", member.getId(), fallbackFeelingList.size());
            } else {
                log.warn("[Fallback 추천] memberId={} 날씨 예보 데이터가 없어 체감온도 그래프를 생성할 수 없습니다. region={}, date={}, hours={}",
                        member.getId(), regionName, targetDate, targetHours);
            }
        } catch (Exception e) {
            log.error("[Fallback 추천] memberId={} 날씨 예보 조회 중 오류 발생: {}", member.getId(), e.getMessage(), e);
        }
        return fallbackFeelingList;
    }

    private RecommendPredictDto makeResultForPredict(Closet closet, ClothingRecommendation recommendation, Member member) {
        try {
            List<Integer> upperTypeList = makeUpperList(closet, recommendation.getTops());
            List<Integer> outerTypeList = makeOuterList(closet, recommendation.getOuters());

            List<WeatherFeelingDto> weatherFeelingDto = makeWeatherFeeling(recommendation.getPredictionMap(), recommendation);

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

    private List<Integer> makeOuterList(Closet closet, List<Integer> outers) {
        if (outers.isEmpty()) return new ArrayList<>();

        Set<Long> ownedClothTypes = closet.getOuterList().stream()
                .filter(Outer::isActive)
                .map(Outer::getOuterType)
                .filter(Objects::nonNull)
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
                .filter(Upper::isActive)
                .map(Upper::getUpperType)
                .filter(Objects::nonNull)
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

    private List<WeatherFeelingDto> makeWeatherFeeling(Map<String, Integer> predictionMap,
                                                       ClothingRecommendation recommendation) {
        List<WeatherFeelingDto> feelingList = new ArrayList<>();
        LocalDate forecastDate = recommendation.getDate();
        String regionName = recommendation.getRegionName();

        List<Integer> targetHours = List.of(9, 12, 15, 18, 21);

        List<WeatherForecast> forecasts = weatherForecastRepository
                .findByRegionNameAndForecastDateAndHourInOrderByHourAsc(regionName, forecastDate, targetHours);

        Map<String, Integer> safePredictionMap = Optional.ofNullable(predictionMap).orElseGet(Collections::emptyMap);
        final int DEFAULT_FEELING = 2;

        for (WeatherForecast forecast : forecasts) {
            int hour = forecast.getHour();
            int feeling = safePredictionMap.getOrDefault(String.valueOf(hour), DEFAULT_FEELING);

            WeatherFeelingDto dto = WeatherFeelingDto.builder()
                    .date(forecast.getForecastDate())
                    .time(hour)
                    .feeling(feeling)
                    .temperature(forecast.getTemperature())
                    .build();
            feelingList.add(dto);

            if (feeling == DEFAULT_FEELING && !safePredictionMap.containsKey(String.valueOf(hour))) {
                log.debug("AI 체감온도 예측값 없음. 기본값(2) 사용: region={}, date={}, hour={}", regionName, forecastDate, hour);
            }
        }

        if (forecasts.isEmpty()) {
            log.warn("날씨 예보 데이터 없음: region={}, date={}, hours={}", regionName, forecastDate, targetHours);
        }
        return feelingList;
    }

    private List<ClothingRecommendation> getModelPrediction(Long id, LocalDate now) {
        return clothingRecommendationRepository.findByMemberIdAndDate(id, now);
    }

    @Transactional
    public void save(ModelClothingRecommendationDto dto, Member member) {
        try {
            List<ModelRecommendationResult> results = Optional.ofNullable(dto.getResult())
                    .orElseGet(ArrayList::new);

            if (results.isEmpty()) {
                log.info("[INFO] 모델 예측 결과가 없습니다. 기존 데이터 유지, memberId={}", member.getId());
                return;
            }

            for (ModelRecommendationResult result : results) {
                ClothingRecommendation recommendation = ClothingRecommendation.builder()
                        .memberId(dto.getUserId())
                        .regionName(member.getRegionName() != null ? member.getRegionName() : "서울특별시 용산구")
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