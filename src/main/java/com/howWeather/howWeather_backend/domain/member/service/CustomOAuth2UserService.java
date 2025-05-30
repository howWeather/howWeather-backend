package com.howWeather.howWeather_backend.domain.member.service;

import com.howWeather.howWeather_backend.domain.member.dto.GoogleUserInfo;
import com.howWeather.howWeather_backend.domain.member.dto.KakaoUserInfo;
import com.howWeather.howWeather_backend.domain.member.dto.OAuth2UserInfo;
import com.howWeather.howWeather_backend.domain.member.entity.LoginType;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.member.repository.MemberRepository;
import com.howWeather.howWeather_backend.global.custom.CustomOAuth2User;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.hierarchicalroles.CycleInRoleHierarchyException;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final RestTemplate restTemplate;
    private final MemberRepository memberRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "kakao" or "google"
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2UserInfo userInfo = getOAuth2UserInfo(registrationId, attributes);
        log.info("{} 로그인 사용자 정보: {}", registrationId, attributes);

        String loginId = registrationId.substring(0, 1).toUpperCase() + registrationId.substring(1) + "_" + userInfo.getId();

        Member member = memberRepository.findByLoginId(loginId)
                .orElseGet(() -> registerNewUser(registrationId, loginId, userInfo));

        return new CustomOAuth2User(member, attributes, "id");
    }

    private OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "kakao" -> new KakaoUserInfo(attributes);
            case "google" -> new GoogleUserInfo(attributes);
            default -> throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + registrationId);
        };
    }

    private Member registerNewUser(String registrationId, String loginId, OAuth2UserInfo userInfo) {
        Member member = Member.builder()
                .loginId(loginId)
                .password(UUID.randomUUID().toString())
                .email(userInfo.getEmail())
                .nickname(userInfo.getNickname())
                .loginType(LoginType.valueOf(registrationId.toUpperCase()))
                .roles(List.of("ROLE_USER"))
                .constitution(1)
                .ageGroup(1)
                .gender(1)
                .sensitivity(1)
                .isDeleted(false)
                .build();
        return memberRepository.save(member);
    }

    public void unlinkKakao(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://kapi.kakao.com/v1/user/unlink", entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("카카오 연결 끊기 실패");
        }
    }

    public void unlinkGoogle(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("token", accessToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://oauth2.googleapis.com/revoke", request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("구글 연결 끊기 실패");
        }
    }

}
