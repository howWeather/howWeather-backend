package com.howWeather.howWeather_backend.global.jwt;

import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
public class RefreshToken {
    @Id
    private String username;
    private String refreshToken;
}
