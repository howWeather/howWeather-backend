package com.howWeather.howWeather_backend.domain.weather.scheduler;

import com.howWeather.howWeather_backend.domain.weather.repository.WeatherForecastRepository;
import com.howWeather.howWeather_backend.domain.weather.service.WeatherService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@AllArgsConstructor
@Slf4j
public class WeatherScheduler {
    private final WeatherService weatherService;
    private final WeatherForecastRepository weatherForecastRepository;

    /**
     * 매일 오전 8시 30분에 전국 실시간 오전 날씨 데이터를 가져옵니다.
     */
    @Scheduled(cron = "0 30 8 * * *", zone = "Asia/Seoul")
    public void fetchMorningWeather() {
        log.info("오전 날씨를 가져옵니다. 시간: {}", LocalDateTime.now());
        weatherService.fetchAllRegionsWeather(1);
    }

    /**
     * 매일 오후 1시 30분에 전국 실시간 오후 날씨 데이터를 가져옵니다.
     */
    @Scheduled(cron = "0 30 13 * * *", zone = "Asia/Seoul")
    public void fetchAfternoonWeather() {
        log.info("오후 날씨를 가져옵니다. 시간: {}", LocalDateTime.now());
        weatherService.fetchAllRegionsWeather(2);
    }

    /**
     * 매일 저녁 7시 30분에 실시간 저녁 날씨 데이터를 가져옵니다.
     */
    @Scheduled(cron = "0 30 19 * * *", zone = "Asia/Seoul")
    public void fetchEveningWeather() {
        log.info("저녁 날씨를 가져옵니다. 시간: {}", LocalDateTime.now());
        weatherService.fetchAllRegionsWeather(3);
    }

    /**
     * 매일 오전 6시에 오늘 이전의 전국 실시간 데이터를 삭제합니다.
     */
    @Scheduled(cron = "0 0 6 * * *", zone = "Asia/Seoul")
    public void deleteOldWeatherData() {
        log.info("전날 날씨 데이터를 삭제합니다. 시간: {}", LocalDateTime.now());
        weatherService.deleteOldWeather();
    }

    /**
     * 매일 오전 4시 30분에 ai 학습에 이용될 오늘의 예보 데이터를 가져옵니다.
     */
    @Scheduled(cron = "0 30 4 * * *", zone = "Asia/Seoul")
    public void fetchDailyOneCallWeather() {
        log.info("Forecast API로 용산구를 포함한 사용자가 예보를 희망하는 지역의 날씨 예보를 가져옵니다. 시간: {}", LocalDateTime.now());
        weatherService.fetchHourlyForecast();
    }

    /**
     * 매일 오전 7시에 오늘 이전의 예보 데이터를 삭제합니다.
     */
    @Transactional
    @Scheduled(cron = "0 0 7 * * *", zone = "Asia/Seoul")
    public void deleteOldForecastData() {
        LocalDate today = LocalDate.now();
        weatherForecastRepository.deleteByForecastDateBefore(today);
        log.info("오늘 이전의 예보 데이터를 모두 삭제했습니다. 기준 날짜: {}", today);
    }
}
