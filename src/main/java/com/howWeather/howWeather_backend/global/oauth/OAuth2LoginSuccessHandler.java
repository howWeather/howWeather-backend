package com.howWeather.howWeather_backend.global.oauth;

import com.howWeather.howWeather_backend.global.jwt.JwtToken;
import com.howWeather.howWeather_backend.global.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        JwtToken token = jwtTokenProvider.generateToken(authentication);

        String accessToken = token.getAccessToken();
        String refreshToken = token.getRefreshToken();

        String redirectUri = UriComponentsBuilder
                .fromUriString("com.howweather.client://oauth")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUriString();

        response.setStatus(HttpServletResponse.SC_FOUND);
        response.setHeader("Location", redirectUri);
    }

}
