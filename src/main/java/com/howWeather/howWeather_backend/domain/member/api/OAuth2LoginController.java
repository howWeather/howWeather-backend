package com.howWeather.howWeather_backend.domain.member.api;

import com.howWeather.howWeather_backend.domain.member.service.KakaoOAuthService;
import com.howWeather.howWeather_backend.global.Response.ApiResponse;
import com.howWeather.howWeather_backend.global.jwt.JwtToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth2")
public class OAuth2LoginController {

    private final KakaoOAuthService kakaoOAuthService;

    @GetMapping("/kakao-login")
    public ResponseEntity<ApiResponse<JwtToken>> kakaoLogin(@RequestParam String code) {
        JwtToken token = kakaoOAuthService.loginWithKakao(code);
        return ApiResponse.loginSuccess(HttpStatus.OK, token, token.getAccessToken());
    }
}
