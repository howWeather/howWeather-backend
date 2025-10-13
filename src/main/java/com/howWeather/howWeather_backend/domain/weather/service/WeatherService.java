package com.howWeather.howWeather_backend.domain.weather.service;

import com.howWeather.howWeather_backend.domain.ai_model.dto.WeatherPredictDto;
import com.howWeather.howWeather_backend.domain.weather.dto.WeatherSimpleDto;
import com.howWeather.howWeather_backend.domain.weather.entity.Region;
import com.howWeather.howWeather_backend.domain.weather.entity.Weather;
import com.howWeather.howWeather_backend.domain.weather.entity.WeatherForecast;
import com.howWeather.howWeather_backend.domain.weather.repository.RegionRepository;
import com.howWeather.howWeather_backend.domain.weather.repository.WeatherForecastRepository;
import com.howWeather.howWeather_backend.domain.weather.repository.WeatherRepository;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class WeatherService {
    private final WeatherRepository weatherRepository;
    private final WeatherApiClient weatherApiClient;
    private final RegionRepository regionRepository;
    private final WeatherForecastRepository weatherForecastRepository;

    @Transactional
    public void fetchAllRegionsWeather(int timeSlot) {
        List<Region> regions = regionRepository.findAll();
        for (Region region : regions) {
            try {
                Weather weatherData = weatherApiClient.fetchWeather(region.getLat(), region.getLon(), region.getName(), timeSlot);
                if (weatherData != null) {
                    weatherRepository.save(weatherData);
                }
            } catch (Exception e) {
                throw new CustomException(ErrorCode.WEATHER_API_CALL_ERROR, "날씨 정보를 가져오는 중 오류가 발생했습니다.");
            }
        }
    }

    @Transactional
    public void deleteOldWeather() {
        weatherRepository.deleteByDateBeforeEqual(LocalDate.now().minusDays(1));
    }


    @Transactional(readOnly = true)
    public double getTemperature(WeatherSimpleDto dto) {
        try{
            LocalDate localDate = LocalDate.parse(dto.getDate());
            return weatherRepository.findByRegionNameAndDateAndTimeSlot(dto.getCity(), localDate, dto.getTimeSlot())
                    .map(Weather::getTemperature)
                    .orElseThrow(() -> new CustomException(ErrorCode. REGION_AND_TIME_NOT_FOUND));
        } catch (CustomException e){
            throw  e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.WEATHER_API_CALL_ERROR, "날씨 정보를 가져오는 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public void fetchHourlyForecast() {
        List<Region> regions = regionRepository.findAll();
        LocalDate baseDate = LocalDate.now();

        weatherForecastRepository.deleteByForecastDateGreaterThanEqual(baseDate);

        List<WeatherForecast> allForecasts = new ArrayList<>();

        for (Region region : regions) {
            double lat = region.getLat();
            double lon = region.getLon();
            String regionName = region.getName();

            List<WeatherPredictDto> forecasts = null;
            try {
                forecasts = weatherApiClient.fetchForecast(lat, lon);

                if (forecasts == null || forecasts.isEmpty()) {
                    log.warn("[날씨 API 경고] 지역 {}에 대해 유효한 예보 데이터가 없어 저장 생략.", regionName);
                    continue;
                }

            } catch (Exception e) {
                log.error("[날씨 API 오류] 지역 {}의 예보 데이터 가져오기 실패: {}", regionName, e.getMessage());
                continue;
            }

            int cnt = 0;
            for (WeatherPredictDto dto : forecasts) {
                LocalDate forecastDate = baseDate.plusDays(cnt / 5);

                WeatherForecast entity = WeatherForecast.builder()
                        .regionName(regionName)
                        .forecastDate(forecastDate)
                        .hour(dto.getHour())
                        .temperature(dto.getTemperature())
                        .humidity(dto.getHumidity())
                        .windSpeed(dto.getWindSpeed())
                        .precipitation(dto.getPrecipitation())
                        .cloudAmount(dto.getCloudAmount())
                        .feelsLike(dto.getFeelsLike())
                        .build();

                allForecasts.add(entity);
                cnt++;
            }
        }

        if (!allForecasts.isEmpty()) {
            weatherForecastRepository.saveAll(allForecasts);
            log.info("[날씨 예보 저장 완료] 총 {}개의 예보 데이터가 DB에 저장되었습니다.", allForecasts.size());
        } else {
            log.warn("[날씨 예보 저장 실패] 모든 지역에서 API 호출에 실패하여 저장된 데이터가 없습니다.");
        }
    }
}
