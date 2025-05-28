package com.howWeather.howWeather_backend.domain.member.service;

import com.howWeather.howWeather_backend.domain.member.dto.KakaoUserInfo;
import com.howWeather.howWeather_backend.domain.member.dto.OAuthToken;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.member.repository.MemberRepository;
import com.howWeather.howWeather_backend.global.jwt.JwtToken;
import com.howWeather.howWeather_backend.global.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService {
    private final RestTemplate restTemplate;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String userInfoUri;

    public JwtToken loginWithKakao(String code) {
        OAuthToken oauthToken = getToken(code);
        KakaoUserInfo userInfo = getUserInfo(oauthToken.getAccessToken());

        String kakaoLoginId = "Kakao_" + userInfo.getId();
        Member member = memberRepository.findByLoginId(kakaoLoginId)
                .orElseGet(() -> registerNewKakaoMember(kakaoLoginId, userInfo));

        Authentication authentication = createAuthenticationForMember(member);
        return jwtTokenProvider.generateToken(authentication);
    }

    private Member registerNewKakaoMember(String loginId, KakaoUserInfo userInfo) {
        Member member = Member.builder()
                .loginId(loginId)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .email(userInfo.getEmail())
                .nickname(userInfo.getNickname())
                .constitution(1)
                .ageGroup(1)
                .gender(1)
                .sensitivity(1)
                .roles(List.of("ROLE_USER"))
                .isDeleted(false)
                .build();
        return memberRepository.save(member);
    }

    private Authentication createAuthenticationForMember(Member member) {
        UserDetails userDetails = User.builder()
                .username(member.getLoginId())
                .password("")
                .roles(member.getRoles().toArray(new String[0]))
                .build();
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private OAuthToken getToken(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<OAuthToken> response = restTemplate.postForEntity(tokenUri, request, OAuthToken.class);
        return response.getBody();
    }

    private KakaoUserInfo getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<KakaoUserInfo> response = restTemplate.exchange(userInfoUri, HttpMethod.GET, entity, KakaoUserInfo.class);
        return response.getBody();
    }
}
