package com.howWeather.howWeather_backend.domain.member.api;

import com.howWeather.howWeather_backend.domain.member.dto.DuplicateCheckDto;
import com.howWeather.howWeather_backend.domain.member.dto.SignupRequestDto;
import com.howWeather.howWeather_backend.domain.member.service.SignupService;
import com.howWeather.howWeather_backend.global.exception.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/signup")
public class SignupController {
    private final SignupService signupService;

    @GetMapping("/email-exist-check")
    public ResponseEntity<?> isEmailExist(@RequestBody DuplicateCheckDto dto) {
        boolean emailAlreadyExist = signupService.isEmailAlreadyExist(dto.getData());

        if (emailAlreadyExist)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 이메일입니다.");

        return ResponseEntity.ok("사용 가능한 이메일입니다.");
    }

    @GetMapping("/loginid-exist-check")
    public ResponseEntity<?> isLoginIdExist(@RequestBody DuplicateCheckDto dto) {
        boolean loginIdAlreadyExist = signupService.isLoginIdAlreadyExist(dto.getData());

        if (loginIdAlreadyExist)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 아아디입니다.");
        return ResponseEntity.ok("사용 가능한 아이디입니다.");
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        try {
            signupService.signup(signupRequestDto);
            return ResponseEntity.ok("회원가입에 성공하였습니다!");
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }
}