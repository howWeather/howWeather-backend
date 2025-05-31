package com.howWeather.howWeather_backend.domain.member.service;

import com.howWeather.howWeather_backend.domain.member.entity.LoginType;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.member.repository.MemberRepository;
import com.howWeather.howWeather_backend.global.custom.CustomUserDetailsService;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import com.howWeather.howWeather_backend.global.exception.LoginException;
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
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

    private final RestTemplate restTemplate;
    private final MemberRepository memberRepository;
    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public JwtToken loginWithGoogleAccessToken(String googleAccessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(googleAccessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v3/userinfo",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new CustomException(ErrorCode.GOOGLE_INVALID_ACCESS_TOKEN);
            }

            Map<String, Object> body = response.getBody();
            String email = (String) body.get("email");
            String name = (String) body.get("name");
            String sub = (String) body.get("sub");

            String loginId = "Google_" + sub;

            Member member = memberRepository.findByLoginId(loginId)
                    .orElseGet(() -> registerNewGoogleUser(loginId, email, name));

            UserDetails userDetails = userDetailsService.loadUserByUsername(member.getLoginId());
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            return jwtTokenProvider.generateToken(authentication);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new LoginException(ErrorCode.UNKNOWN_ERROR, "구글 소셜 로그인 중 오류가 발생했습니다.");
        }
    }

    private Member registerNewGoogleUser(String loginId, String email, String name) {
        Member member = Member.builder()
                .loginId(loginId)
                .email(email)
                .nickname(name)
                .password(UUID.randomUUID().toString())
                .loginType(LoginType.GOOGLE)
                .roles(List.of("ROLE_USER"))
                .constitution(1)
                .ageGroup(1)
                .gender(1)
                .sensitivity(-1)
                .isDeleted(false)
                .build();
        return memberRepository.save(member);
    }
}
