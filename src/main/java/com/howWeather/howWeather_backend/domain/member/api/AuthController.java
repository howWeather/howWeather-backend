package com.howWeather.howWeather_backend.domain.member.api;

import com.howWeather.howWeather_backend.domain.member.dto.DuplicateCheckDto;
import com.howWeather.howWeather_backend.domain.member.dto.LoginRequestDto;
import com.howWeather.howWeather_backend.domain.member.dto.SignupRequestDto;
import com.howWeather.howWeather_backend.domain.member.service.AuthService;
import com.howWeather.howWeather_backend.global.exception.UserAlreadyExistsException;
import com.howWeather.howWeather_backend.global.jwt.CheckAuthenticatedUser;
import com.howWeather.howWeather_backend.global.jwt.JwtToken;
import com.howWeather.howWeather_backend.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        try {
            authService.signup(signupRequestDto);
            return ResponseEntity.ok("회원가입에 성공하였습니다!");
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }

    @PostMapping("/login")
    public JwtToken login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        return authService.login(loginRequestDto);
    }

    @PostMapping("/logout")
    @CheckAuthenticatedUser
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String accessToken,
                                    @RequestHeader("Refresh-Token") String refreshToken) {

        String accessTokenValue = accessToken.startsWith("Bearer ") ? accessToken.substring(7).trim() : accessToken.trim();
        String refreshTokenValue = refreshToken.startsWith("Bearer ") ? refreshToken.substring(7).trim() : refreshToken.trim();
        try {
            authService.logout(accessTokenValue, refreshTokenValue);
            return ResponseEntity.ok("로그아웃 성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("로그아웃 처리 중 오류 발생" + e.getMessage());
        }
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissueToken(@RequestHeader("Authorization") String refreshToken) {
        if (refreshToken == null || !refreshToken.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("리프레시 토큰을 제공해주세요.");
        }

        String token = refreshToken.substring(7); // "Bearer " 제거

        try {
            JwtToken newTokens = jwtTokenProvider.reissueAccessToken(token);
            return ResponseEntity.ok(newTokens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰 재발급 실패: " + e.getMessage());
        }
    }

    @GetMapping("/email-exist-check")
    public ResponseEntity<?> isEmailExist(@RequestBody DuplicateCheckDto dto) {
        boolean emailAlreadyExist = authService.isEmailAlreadyExist(dto.getData());

        if (emailAlreadyExist)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 이메일입니다.");

        return ResponseEntity.ok("사용 가능한 이메일입니다.");
    }

    @GetMapping("/loginid-exist-check")
    public ResponseEntity<?> isLoginIdExist(@RequestBody DuplicateCheckDto dto) {
        boolean loginIdAlreadyExist = authService.isLoginIdAlreadyExist(dto.getData());

        if (loginIdAlreadyExist)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 아아디입니다.");
        return ResponseEntity.ok("사용 가능한 아이디입니다.");
    }
}
