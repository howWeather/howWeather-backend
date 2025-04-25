package com.howWeather.howWeather_backend.global.jwt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
public class JwtToken {
    private String grantType;
    private String accessToken;
    private String refreshToken;
}
