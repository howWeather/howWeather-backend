package com.howWeather.howWeather_backend.domain.weather.scheduler;

import com.howWeather.howWeather_backend.domain.weather.repository.WeatherForecastRepository;
import com.howWeather.howWeather_backend.domain.weather.service.WeatherService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@AllArgsConstructor
@Slf4j
public class WeatherScheduler {
    private final WeatherService weatherService;
    private final WeatherForecastRepository weatherForecastRepository;

    @Scheduled(cron = "0 30 8 * * *", zone = "Asia/Seoul") // 오전 8시 30분
    public void fetchMorningWeather() {
        log.info("오전 날씨를 가져옵니다. 시간: {}", LocalDateTime.now());
        weatherService.fetchAllRegionsWeather(1);
    }

    @Scheduled(cron = "0 30 13 * * *", zone = "Asia/Seoul") // 오후 1시 30분
    public void fetchAfternoonWeather() {
        log.info("오후 날씨를 가져옵니다. 시간: {}", LocalDateTime.now());
        weatherService.fetchAllRegionsWeather(2);
    }

    @Scheduled(cron = "0 30 19 * * *", zone = "Asia/Seoul") // 저녁 7시 30분
    public void fetchEveningWeather() {
        log.info("저녁 날씨를 가져옵니다. 시간: {}", LocalDateTime.now());
        weatherService.fetchAllRegionsWeather(3);
    }

    @Scheduled(cron = "0 0 6 * * *", zone = "Asia/Seoul") // 매일 오전 6시에 전날 데이터 삭제
    public void deleteOldWeatherData() {
        log.info("전날 날씨 데이터를 삭제합니다. 시간: {}", LocalDateTime.now());
        weatherService.deleteYesterdayWeather();
    }

    @Scheduled(cron = "0 0 5 * * *", zone = "Asia/Seoul") // 매일 오전 5시에 예보 데이터
    public void fetchDailyOneCallWeather() {
        log.info("매일 오전 5시에 Forecast API로 용산구의 날씨 예보를 가져옵니다. 시간: {}", LocalDateTime.now());
        weatherService.fetchHourlyForecast();
    }

    @Scheduled(cron = "0 0 7 * * *", zone = "Asia/Seoul") // 매일 오전 7시
    public void deleteOldForecastData() {
        weatherForecastRepository.deleteByForecastDateBefore(LocalDate.now());
        log.info("오늘 이전의 예보 데이터를 모두 삭제했습니다. 기준 날짜: {}", LocalDate.now());
    }
}
