package com.howWeather.howWeather_backend.domain.member.api;

import com.howWeather.howWeather_backend.domain.member.dto.LoginDto;
import com.howWeather.howWeather_backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final MemberRepository memberRepository;

    @PostMapping("/login")
    public ResponseEntity<?> isEmailExist(@RequestBody LoginDto dto) {
        // TODO : 기능 구현
        return ResponseEntity.ok("로그인에 성공하였습니다.");
    }
}
