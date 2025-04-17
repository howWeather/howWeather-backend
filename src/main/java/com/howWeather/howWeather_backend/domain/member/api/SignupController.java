package com.howWeather.howWeather_backend.domain.member.api;

import com.howWeather.howWeather_backend.domain.member.service.SignupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/signup")
public class SignupController {
    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @GetMapping("/email-exist-check")
    public ResponseEntity<?> isEmailExist(@RequestParam String email) {
        boolean emailAlreadyExist = signupService.isEmailAlreadyExist(email);

        if (emailAlreadyExist)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 이메일입니다.");

        return ResponseEntity.ok("사용 가능한 이메일입니다.");
    }

    @GetMapping("/loginid-exist-check")
    public ResponseEntity<?> isLoginIdExist(@RequestParam String loginId) {
        boolean loginIdAlreadyExist = signupService.isLoginIdAlreadyExist(loginId);

        if (loginIdAlreadyExist)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 아아디입니다.");
        return ResponseEntity.ok("사용 가능한 아이디입니다.");
    }
}
