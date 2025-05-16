package com.howWeather.howWeather_backend.domain.weather.scheduler;

import com.howWeather.howWeather_backend.domain.weather.service.WeatherService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@AllArgsConstructor
@Slf4j
public class WeatherScheduler {
    private final WeatherService weatherService;
    @Scheduled(cron = "0 30 10 * * *", zone = "Asia/Seoul") // 오전 10시 30분
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
}
