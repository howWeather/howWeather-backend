package com.howWeather.howWeather_backend.domain.member.api;

import com.howWeather.howWeather_backend.domain.member.dto.EmailDto;
import com.howWeather.howWeather_backend.domain.member.service.EmailVerificationService;
import com.howWeather.howWeather_backend.global.Response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/email")
public class EmailVerificationController {
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/code")
    public ResponseEntity<ApiResponse<String>> signup(@Valid @RequestBody EmailDto emailDto) {
        emailVerificationService.sendVerificationCode(emailDto.getEmail());
        return ApiResponse.success(HttpStatus.OK, "인증 코드가 전송되었습니다. 메일함을 확인해주세요!");
    }
}
