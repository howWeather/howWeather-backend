package com.howWeather.howWeather_backend.domain.member.service;

import com.howWeather.howWeather_backend.global.jwt.JwtToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface SocialOAuthService {
    JwtToken login(OAuth2User oAuth2User);
    boolean supports(String registrationId);
}
