package com.howWeather.howWeather_backend.domain.weather.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;

@Entity
public class Weather {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String regionName;
    private double latitude;
    private double longitude;

    private LocalDate date;
    private int timeSlot;

    private double temperature;
    private double humidity;
    private double windSpeed;
    private double precipitation;
    private double cloudiness;
    private double feelsLike;
    private String weatherDescription;
}
