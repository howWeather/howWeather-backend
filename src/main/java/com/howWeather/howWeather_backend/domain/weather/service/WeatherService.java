package com.howWeather.howWeather_backend.domain.weather.service;

import com.howWeather.howWeather_backend.domain.weather.dto.WeatherSimpleDto;
import com.howWeather.howWeather_backend.domain.weather.entity.Region;
import com.howWeather.howWeather_backend.domain.weather.entity.Weather;
import com.howWeather.howWeather_backend.domain.weather.repository.RegionRepository;
import com.howWeather.howWeather_backend.domain.weather.repository.WeatherRepository;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class WeatherService {
    private final WeatherRepository weatherRepository;
    private final WeatherApiClient weatherApiClient;
    private final RegionRepository regionRepository;

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
                throw new CustomException(ErrorCode.API_CALL_ERROR, "날씨 정보를 가져오는 중 오류가 발생했습니다.");
            }
        }
    }

    @Transactional
    public void deleteYesterdayWeather() {
        weatherRepository.deleteByDate(LocalDate.now().minusDays(1));
    }

    @Transactional(readOnly = true)
    public double getTemperature(WeatherSimpleDto dto) {
        try{
            LocalDate localDate = LocalDate.parse(dto.getDate());
            return weatherRepository.findByRegionNameAndDateAndTimeSlot(dto.getCity(), localDate, dto.getTimeSlot())
                    .map(Weather::getTemperature)
                    .orElseThrow(() -> new CustomException(ErrorCode.REGION_NOT_FOUND, "해당 지역과 시간대의 온도는 현재 제공하지 않습니다."));
        } catch (CustomException e){
            throw  e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.API_CALL_ERROR, "날씨 정보를 가져오는 중 오류가 발생했습니다.");
        }
    }
}
