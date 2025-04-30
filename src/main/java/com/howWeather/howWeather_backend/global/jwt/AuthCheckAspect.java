package com.howWeather.howWeather_backend.global.jwt;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthCheckAspect {

    private final JwtTokenProvider jwtTokenProvider;

    @Around("@annotation(com.howWeather.howWeather_backend.global.jwt.CheckAuthenticatedUser)")
    public Object checkAuthenticatedUser(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String token = JwtTokenProvider.extractTokenFromRequest();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "유효하지 않은 토큰입니다."));
        }

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인된 사용자가 없습니다."));
        }

        String authUsername = authentication.getName();
        String tokenUsername = jwtTokenProvider.getUsername(token);

        if (!authUsername.equals(tokenUsername)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "사용자 권한이 일치하지 않습니다."));
        }
        return joinPoint.proceed();
    }
}
