package com.howWeather.howWeather_backend.domain.alarm.api;

import com.howWeather.howWeather_backend.domain.alarm.dto.FcmTokenRequestDto;
import com.howWeather.howWeather_backend.domain.alarm.service.FcmTokenService;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fcm-token")
@RequiredArgsConstructor
public class FcmTokenController {
    private final FcmTokenService fcmTokenService;

    @PostMapping("/save")
    public ResponseEntity<String> saveToken(@RequestBody FcmTokenRequestDto request,
                                          @RequestHeader("Authorization") String accessTokenHeader,
                                          @AuthenticationPrincipal Member member) {
        Long memberId = member.getId();
        fcmTokenService.saveToken(memberId, request.getToken());
        return ResponseEntity.ok("FCM 토큰이 성공적으로 저장되었습니다.");
    }
}