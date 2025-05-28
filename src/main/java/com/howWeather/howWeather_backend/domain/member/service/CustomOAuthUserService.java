package com.howWeather.howWeather_backend.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomOAuthUserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final List<SocialOAuthService> socialOAuthServices;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        SocialOAuthService delegate = socialOAuthServices.stream()
                .filter(s -> s.supports(registrationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 소셜 로그인: " + registrationId));

        delegate.processOAuth2User(oAuth2User);
        return oAuth2User;
    }

}
