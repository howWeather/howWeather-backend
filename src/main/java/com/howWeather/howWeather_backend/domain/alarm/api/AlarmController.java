package com.howWeather.howWeather_backend.domain.alarm.api;

import com.howWeather.howWeather_backend.domain.alarm.dto.FcmAlarmPreferenceDto;
import com.howWeather.howWeather_backend.domain.alarm.dto.FcmTokenRequestDto;
import com.howWeather.howWeather_backend.domain.alarm.service.FcmAlarmPreferenceService;
import com.howWeather.howWeather_backend.domain.alarm.service.FcmTokenService;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alarm")
@RequiredArgsConstructor
public class AlarmController {
    private final FcmTokenService fcmTokenService;
    private final FcmAlarmPreferenceService fcmAlarmPreferenceService;

    @PostMapping("/token-save")
    public ResponseEntity<String> saveToken(@RequestBody FcmTokenRequestDto request,
                                          @RequestHeader("Authorization") String accessTokenHeader,
                                          @AuthenticationPrincipal Member member) {
        Long memberId = member.getId();
        fcmTokenService.saveToken(memberId, request.getToken());
        return ResponseEntity.ok("FCM 토큰이 성공적으로 저장되었습니다.");
    }

    @DeleteMapping("/token-delete")
    public ResponseEntity<Void> deleteToken(@RequestBody FcmTokenRequestDto request,
                                            @RequestHeader("Authorization") String accessTokenHeader,
                                            @AuthenticationPrincipal Member member) {
        Long memberId = member.getId();
        fcmTokenService.deleteToken(memberId, request.getToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<FcmAlarmPreferenceDto> getPreference(@RequestHeader("Authorization") String accessTokenHeader,
                                                               @AuthenticationPrincipal Member member) {
        FcmAlarmPreferenceDto dto = fcmAlarmPreferenceService.getPreference(member);
        return ResponseEntity.ok(dto);
    }
}