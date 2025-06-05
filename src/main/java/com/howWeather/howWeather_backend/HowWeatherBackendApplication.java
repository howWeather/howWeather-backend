package com.howWeather.howWeather_backend;

import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
public class HowWeatherBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(HowWeatherBackendApplication.class, args);
	}

}
