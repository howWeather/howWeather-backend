package com.howWeather.howWeather_backend.global.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.howWeather.howWeather_backend.global.jwt.JwtToken;
import com.howWeather.howWeather_backend.global.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        JwtToken token = jwtTokenProvider.generateToken(authentication);

        Map<String, Object> result = new HashMap<>();
        result.put("grantType", "Bearer");
        result.put("accessToken", token.getAccessToken());
        result.put("refreshToken", token.getRefreshToken());

        Map<String, Object> body = new HashMap<>();
        body.put("httpStatus", "OK");
        body.put("success", true);
        body.put("result", result);
        body.put("error", null);

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
