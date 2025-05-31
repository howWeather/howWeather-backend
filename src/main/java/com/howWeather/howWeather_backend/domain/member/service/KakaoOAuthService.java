package com.howWeather.howWeather_backend.domain.member.service;

import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import com.howWeather.howWeather_backend.global.exception.LoginException;
import org.springframework.http.HttpHeaders;
import com.howWeather.howWeather_backend.domain.member.entity.LoginType;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.member.repository.MemberRepository;
import com.howWeather.howWeather_backend.global.custom.CustomUserDetailsService;
import com.howWeather.howWeather_backend.global.jwt.JwtToken;
import com.howWeather.howWeather_backend.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService {
    private final RestTemplate restTemplate;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Transactional
    public JwtToken loginWithKakaoAccessToken(String kakaoAccessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(kakaoAccessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new CustomException(ErrorCode.KAKAO_INVALID_ACCESS_TOKEN);
            }

            Map<String, Object> body = response.getBody();
            Long kakaoId = Long.valueOf(body.get("id").toString());

            Map<String, Object> kakaoAccount = (Map<String, Object>) body.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            String email = (String) kakaoAccount.get("email");
            String nickname = (String) profile.get("nickname");

            String loginId = "Kakao_" + kakaoId;

            Member member = memberRepository.findByLoginId(loginId)
                    .orElseGet(() -> registerNewKakaoUser(loginId, email, nickname));

            UserDetails userDetails = userDetailsService.loadUserByUsername(member.getLoginId());
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            return jwtTokenProvider.generateToken(authentication);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new LoginException(ErrorCode.UNKNOWN_ERROR, "카카오 소셜 로그인 중 오류가 발생했습니다.");
        }
    }

    private Member registerNewKakaoUser(String loginId, String email, String nickname) {
        Member newMember = Member.builder()
                .loginId(loginId)
                .email(email)
                .nickname(nickname)
                .password(UUID.randomUUID().toString())
                .loginType(LoginType.KAKAO)
                .roles(List.of("ROLE_USER"))
                .constitution(1)
                .ageGroup(1)
                .gender(1)
                .sensitivity(-1)
                .isDeleted(false)
                .build();
        return memberRepository.save(newMember);
    }
}
