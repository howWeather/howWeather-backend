package com.howWeather.howWeather_backend.domain.weather.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@EntityListeners(AuditingEntityListener.class)
public class WeatherForecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String regionName;

    private LocalDate forecastDate;

    /**
     * 시간대 (ex: 9, 12, 15, 18, 21)
     */
    private int hour;

    private double temperature;

    private double humidity;

    private double windSpeed;

    private double precipitation;

    private double cloudAmount;

    private double feelsLike;

    @CreatedDate
    private LocalDateTime createdAt;
}
