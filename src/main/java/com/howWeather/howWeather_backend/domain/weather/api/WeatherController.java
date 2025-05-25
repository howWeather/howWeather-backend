package com.howWeather.howWeather_backend.domain.weather.api;

import com.howWeather.howWeather_backend.domain.weather.dto.WeatherSimpleDto;
import com.howWeather.howWeather_backend.domain.weather.service.WeatherService;
import com.howWeather.howWeather_backend.global.Response.ApiResponse;
import com.howWeather.howWeather_backend.global.jwt.CheckAuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/weather")
public class WeatherController {
    private final WeatherService weatherService;

    @GetMapping("/temp")
    @CheckAuthenticatedUser
    public ResponseEntity<ApiResponse<Double>> getTemp(@RequestHeader("Authorization") String accessTokenHeader,
                                                @RequestBody WeatherSimpleDto weatherSimpleDto) {
        double temperature = weatherService.getTemperature(weatherSimpleDto);
        return ApiResponse.success(HttpStatus.OK, temperature);
    }
}
