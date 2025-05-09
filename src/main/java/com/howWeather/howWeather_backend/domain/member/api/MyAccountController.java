package com.howWeather.howWeather_backend.domain.member.api;

import com.howWeather.howWeather_backend.domain.member.dto.ProfileDto;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.member.repository.MemberRepository;
import com.howWeather.howWeather_backend.domain.member.service.AuthService;
import com.howWeather.howWeather_backend.global.jwt.CheckAuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class MyAccountController {
    private final MemberRepository memberRepository;
    private final AuthService authService;

    @GetMapping("/profile")
    @CheckAuthenticatedUser
    public ResponseEntity<ProfileDto> getProfile(@RequestHeader("Authorization") String accessTokenHeader,
                                                              @AuthenticationPrincipal Member member) {
        ProfileDto profile = authService.getProfile(member);
        return ResponseEntity.ok(profile);
    }
}
