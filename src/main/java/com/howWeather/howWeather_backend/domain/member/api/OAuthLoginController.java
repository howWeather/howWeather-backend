package com.howWeather.howWeather_backend.domain.member.api;

import com.howWeather.howWeather_backend.domain.member.dto.GoogleLoginRequest;
import com.howWeather.howWeather_backend.domain.member.dto.KakaoLoginRequest;
import com.howWeather.howWeather_backend.domain.member.service.GoogleOAuthService;
import com.howWeather.howWeather_backend.domain.member.service.KakaoOAuthService;
import com.howWeather.howWeather_backend.global.Response.ApiResponse;
import com.howWeather.howWeather_backend.global.jwt.JwtToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
public class OAuthLoginController {
    private final KakaoOAuthService kakaoOAuthService;
    private final GoogleOAuthService googleOAuthService;

    @PostMapping("/kakao")
    public ResponseEntity<ApiResponse<JwtToken>> kakaoLogin(@RequestBody KakaoLoginRequest request) {
        String kakaoAccessToken = request.getAccessToken();
        JwtToken token = kakaoOAuthService.loginWithKakaoAccessToken(kakaoAccessToken);
        return ApiResponse.loginSuccess(HttpStatus.OK, token, token.getAccessToken());
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<JwtToken>> googleLogin(@RequestBody GoogleLoginRequest request) {
        String googleAccessToken = request.getAccessToken();
        JwtToken token = googleOAuthService.loginWithGoogleAccessToken(googleAccessToken);
        return ApiResponse.loginSuccess(HttpStatus.OK, token, token.getAccessToken());
    }
}
