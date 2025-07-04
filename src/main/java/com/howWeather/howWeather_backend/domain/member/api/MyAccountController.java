package com.howWeather.howWeather_backend.domain.member.api;

import com.howWeather.howWeather_backend.domain.member.dto.NicknameDto;
import com.howWeather.howWeather_backend.domain.member.dto.ProfileChangeIntDto;
import com.howWeather.howWeather_backend.domain.member.dto.ProfileDto;
import com.howWeather.howWeather_backend.domain.member.dto.RegionDto;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.member.repository.MemberRepository;
import com.howWeather.howWeather_backend.domain.member.service.AuthService;
import com.howWeather.howWeather_backend.global.Response.ApiResponse;
import com.howWeather.howWeather_backend.global.jwt.CheckAuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class MyAccountController {
    private final AuthService authService;

    @GetMapping("/profile")
    @CheckAuthenticatedUser
    public ResponseEntity<ProfileDto> getProfile(@RequestHeader("Authorization") String accessTokenHeader,
                                                              @AuthenticationPrincipal Member member) {
        ProfileDto profile = authService.getProfile(member);
        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/update-gender")
    @CheckAuthenticatedUser
    public ResponseEntity<ApiResponse<String>> updateGender(@RequestHeader("Authorization") String accessTokenHeader,
                                                            @RequestBody ProfileChangeIntDto profileChangeDto,
                                                            @AuthenticationPrincipal Member member) {
        authService.updateGender(member, profileChangeDto);
        return ApiResponse.success(HttpStatus.OK, "성별을 성공적으로 수정하였습니다.");
    }

    @PatchMapping("/update-age")
    @CheckAuthenticatedUser
    public ResponseEntity<ApiResponse<String>> updateAgeGroup(@RequestHeader("Authorization") String accessTokenHeader,
                                                            @RequestBody ProfileChangeIntDto profileChangeDto,
                                                            @AuthenticationPrincipal Member member) {
        authService.updateAgeGroup(member, profileChangeDto);
        return ApiResponse.success(HttpStatus.OK, "연령대를 성공적으로 수정하였습니다.");
    }

    @PatchMapping("/update-nickname")
    @CheckAuthenticatedUser
    public ResponseEntity<ApiResponse<String>> updateNickname(@RequestHeader("Authorization") String accessTokenHeader,
                                                              @Valid @RequestBody NicknameDto nicknameDto,
                                                              @AuthenticationPrincipal Member member) {
        authService.updateNickname(member,nicknameDto);
        return ApiResponse.success(HttpStatus.OK, "닉네임을 성공적으로 수정하였습니다.");
    }

    @PatchMapping("/update-constitution")
    @CheckAuthenticatedUser
    public ResponseEntity<ApiResponse<String>> updateConstitution(@RequestHeader("Authorization") String accessTokenHeader,
                                                              @Valid @RequestBody ProfileChangeIntDto profileChangeDto,
                                                              @AuthenticationPrincipal Member member) {
        authService.updateConstitution(member, profileChangeDto);
        return ApiResponse.success(HttpStatus.OK, "체질을 성공적으로 수정하였습니다.");
    }

    @GetMapping("/location")
    @CheckAuthenticatedUser
    public ResponseEntity<ApiResponse<String>> getLocation(@RequestHeader("Authorization") String accessTokenHeader,
                                                           @AuthenticationPrincipal Member member) {
        String location = authService.getLoccation(member);
        return ApiResponse.success(HttpStatus.OK, location);
    }

    @PatchMapping("/update-location")
    @CheckAuthenticatedUser
    public ResponseEntity<ApiResponse<String>> updateLocation(@RequestHeader("Authorization") String accessTokenHeader,
                                                              @Valid @RequestBody RegionDto regionDto,
                                                              @AuthenticationPrincipal Member member) {
        authService.updateLocation(member, regionDto);
        return ApiResponse.success(HttpStatus.OK, "지역을 성공적으로 수정하였습니다.");
    }
}
