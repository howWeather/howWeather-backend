package com.howWeather.howWeather_backend.domain.weather.service;

import com.howWeather.howWeather_backend.domain.ai_model.dto.WeatherPredictDto;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.howWeather.howWeather_backend.domain.weather.entity.Weather;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class WeatherApiClient {
    private final RestTemplate restTemplate;

    private final String WEATHER_API_KEY;

    public WeatherApiClient(RestTemplate restTemplate, @Value("${openweather.api.key}") String WEATHER_API_KEY) {
        this.restTemplate = restTemplate;
        this.WEATHER_API_KEY = WEATHER_API_KEY;
    }

    public Weather fetchWeather(double lat, double lon, String regionName, int timeSlot) {
        String url = buildUrl(lat, lon);
        ResponseEntity<Map> response = fetchWeatherData(url);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new CustomException(ErrorCode.WEATHER_API_CALL_ERROR, "날씨 데이터를 가져오는 데 실패했습니다.");
        }

        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new CustomException(ErrorCode.NO_BODY_ERROR, "날씨 데이터가 비어있습니다.");
        }

        return buildWeather(regionName, timeSlot, lat, lon, body);
    }


    private String buildUrl(double lat, double lon) {
        return String.format("https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s&units=metric&lang=kr", lat, lon, WEATHER_API_KEY);
    }

    private ResponseEntity<Map> fetchWeatherData(String url) {
        try {
            return restTemplate.exchange(url, HttpMethod.GET, null, Map.class);
        } catch (RestClientException e) {
            log.error(e.getMessage());
            throw new CustomException(ErrorCode.WEATHER_API_CALL_ERROR, "날씨 API 호출 실패");
        }
    }

    private Weather buildWeather(String regionName, int timeSlot, double lat, double lon, Map<String, Object> body) {
    Map<String, Object> main = (Map<String, Object>) body.get("main");
        Map<String, Object> wind = (Map<String, Object>) body.get("wind");
        Map<String, Object> clouds = (Map<String, Object>) body.get("clouds");
        Map<String, Object> rain = (Map<String, Object>) body.getOrDefault("rain", new HashMap<>());

        double temperature = extractDouble(main, "temp");
        double humidity = extractDouble(main, "humidity");
        double windSpeed = extractDouble(wind, "speed");
        double cloudiness = extractDouble(clouds, "all");
        double precipitation = extractPrecipitation(rain);
        double feelsLike = extractDouble(main, "feels_like");
        String description = extractWeatherDescription(body);

        return Weather.builder()
                .regionName(regionName)
                .latitude(lat)
                .longitude(lon)
                .temperature(temperature)
                .humidity(humidity)
                .windSpeed(windSpeed)
                .precipitation(precipitation)
                .cloudiness(cloudiness)
                .feelsLike(feelsLike)
                .weatherDescription(description)
                .timeSlot(timeSlot)
                .date(LocalDate.now())
                .build();

    }

    private double extractDouble(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }

    private double extractPrecipitation(Map<String, Object> rain) {
        Object value = rain.get("1h");
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }

    private String extractWeatherDescription(Map<String, Object> body) {
        Object weatherObj = body.get("weather");
        if (weatherObj instanceof List<?> weatherList && !weatherList.isEmpty()) {
            Object firstItem = weatherList.get(0);
            if (firstItem instanceof Map<?, ?> weatherMap) {
                return (String) weatherMap.get("description");
            }
        }
        return "";
    }

    public List<WeatherPredictDto> fetchForecast(double lat, double lon) {
        String url = String.format(
                "https://api.openweathermap.org/data/2.5/forecast?lat=%f&lon=%f&appid=%s&units=metric&lang=kr",
                lat, lon, WEATHER_API_KEY
        );

        ResponseEntity<Map> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, null, Map.class);
        } catch (RestClientException e) {
            log.error("Forecast API 호출 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.WEATHER_API_CALL_ERROR, "Forecast API 호출 실패");
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new CustomException(ErrorCode.WEATHER_API_CALL_ERROR, "Forecast API 호출 실패");
        }

        Map<String, Object> body = response.getBody();
        if (body == null || !body.containsKey("list")) {
            throw new CustomException(ErrorCode.NO_BODY_ERROR, "Forecast API 응답에 list 데이터가 없습니다.");
        }

        List<Map<String, Object>> list = (List<Map<String, Object>>) body.get("list");

        List<Integer> targetHours = List.of(9, 12, 15, 18, 21);
        List<WeatherPredictDto> forecastList = new ArrayList<>();

        for (Map<String, Object> forecast : list) {
            String dtTxt = (String) forecast.get("dt_txt");
            ZonedDateTime dateTime = ZonedDateTime.of(LocalDateTime.parse(dtTxt, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), ZoneId.of("Asia/Seoul"));
            int hour = dateTime.getHour();

            if (targetHours.contains(hour)) {
                Map<String, Object> main = (Map<String, Object>) forecast.get("main");
                Map<String, Object> wind = (Map<String, Object>) forecast.get("wind");
                Map<String, Object> clouds = (Map<String, Object>) forecast.get("clouds");

                double temp = getDoubleValue(main, "temp");
                double feelsLike = getDoubleValue(main, "feels_like");
                double humidity = getDoubleValue(main, "humidity");
                double windSpeed = getDoubleValue(wind, "speed");
                double cloudiness = getDoubleValue(clouds, "all");

                double precipitation = 0.0;
                if (forecast.containsKey("rain")) {
                    Map<String, Object> rain = (Map<String, Object>) forecast.get("rain");
                    precipitation = getDoubleValue(rain, "3h");
                }

                WeatherPredictDto dto = WeatherPredictDto.builder()
                        .hour(hour)
                        .temperature(temp)
                        .humidity(humidity)
                        .windSpeed(windSpeed)
                        .cloudAmount(cloudiness)
                        .feelsLike(feelsLike)
                        .precipitation(precipitation)
                        .build();

                forecastList.add(dto);
            }
        }
        return forecastList;
    }


    private double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return 0.0;
    }
}
