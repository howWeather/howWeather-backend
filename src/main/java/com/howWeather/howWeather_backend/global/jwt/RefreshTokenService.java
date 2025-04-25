package com.howWeather.howWeather_backend.global.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Component
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public void saveRefreshToken(String username, String refreshToken) {
        RefreshToken savedRefreshToken = new RefreshToken(username, refreshToken);
        refreshTokenRepository.save(savedRefreshToken);
    }
}