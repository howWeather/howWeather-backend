package com.howWeather.howWeather_backend.domain.member.api;

import com.howWeather.howWeather_backend.domain.member.dto.DuplicateCheckDto;
import com.howWeather.howWeather_backend.domain.member.dto.LoginRequestDto;
import com.howWeather.howWeather_backend.domain.member.dto.PasswordChangeDto;
import com.howWeather.howWeather_backend.domain.member.dto.SignupRequestDto;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.member.service.AuthService;
import com.howWeather.howWeather_backend.global.Response.ApiResponse;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import com.howWeather.howWeather_backend.global.exception.UserAlreadyExistsException;
import com.howWeather.howWeather_backend.global.jwt.CheckAuthenticatedUser;
import com.howWeather.howWeather_backend.global.jwt.JwtToken;
import com.howWeather.howWeather_backend.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<ApiResponse<String>> signup(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        authService.signup(signupRequestDto);
        return ApiResponse.success(HttpStatus.OK, "회원가입에 성공하였습니다.");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtToken>> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        JwtToken token = authService.login(loginRequestDto);
        return ApiResponse.loginSuccess(HttpStatus.OK, token, token.getAccessToken());
    }

    @PostMapping("/logout")
    @CheckAuthenticatedUser
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String accessTokenHeader,
                                                      @RequestHeader("Refresh-Token") String refreshTokenHeader) {

        String accessToken = extractToken(accessTokenHeader);
        String refreshToken = extractToken(refreshTokenHeader);

        if (!jwtTokenProvider.validateToken(accessToken) || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        authService.logout(accessToken, refreshToken);
        return ApiResponse.success(HttpStatus.OK, "로그아웃에 성공하였습니다.");
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<JwtToken>> reissueToken(@RequestHeader("Authorization") String refreshTokenHeader) {
        if (refreshTokenHeader == null || !refreshTokenHeader.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        String refreshToken = refreshTokenHeader.substring(7).trim();
        JwtToken newTokens = jwtTokenProvider.reissueAccessToken(refreshToken);
        return ApiResponse.success(HttpStatus.OK, newTokens);
    }

    @GetMapping("/email-exist-check")
    public ResponseEntity<ApiResponse<String>> isEmailExist(@RequestParam("email") String email) {
        boolean emailAlreadyExist = authService.isEmailAlreadyExist(email);

        if (emailAlreadyExist) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        return ApiResponse.success(HttpStatus.OK, "사용 가능한 이메일입니다.");
    }


    @GetMapping("/loginid-exist-check")
    public ResponseEntity<ApiResponse<String>> isLoginIdExist(@RequestParam("loginId") String loginId) {
        boolean loginIdAlreadyExist = authService.isLoginIdAlreadyExist(loginId);

        if (loginIdAlreadyExist) {
            throw new CustomException(ErrorCode.LOGIN_ID_ALREADY_EXISTS);
        }
        return ApiResponse.success(HttpStatus.OK, "사용 가능한 아이디입니다.");
    }

    @PostMapping("/update-password")
    @CheckAuthenticatedUser
    public ResponseEntity<ApiResponse<String>> passwordChange(@RequestHeader("Authorization") String accessTokenHeader,
                                                              @RequestHeader("Refresh-Token") String refreshTokenHeader,
                                                              @Valid @RequestBody PasswordChangeDto dto,
                                                              @AuthenticationPrincipal Member member) {
        authService.changePassword(member, dto);
        logout(accessTokenHeader, refreshTokenHeader);
        return ApiResponse.success(HttpStatus.OK, "비밀번호를 성공적으로 변경하였습니다. 재로그인하시기 바랍니다.");
    }

    @DeleteMapping("/delete")
    @CheckAuthenticatedUser
    public ResponseEntity<Void> withdrawMember(@RequestHeader("Authorization") String accessTokenHeader,
                                               @RequestHeader("Refresh-Token") String refreshTokenHeader,
                                               @AuthenticationPrincipal Member member) {

        String accessToken = extractToken(accessTokenHeader);
        String refreshToken = extractToken(refreshTokenHeader);

        authService.withdraw(member, accessToken, refreshToken);

        return ResponseEntity.noContent().build();
    }

    private String extractToken(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        return header.substring(7).trim();
    }
}
