package com.howWeather.howWeather_backend.domain.member.api;

import com.howWeather.howWeather_backend.domain.member.dto.NicknameDto;
import com.howWeather.howWeather_backend.domain.member.dto.ProfileChangeIntDto;
import com.howWeather.howWeather_backend.domain.member.dto.ProfileDto;
import com.howWeather.howWeather_backend.domain.member.dto.RegionDto;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.member.service.MyAccountService;
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
    private final MyAccountService myAccountService;

    @GetMapping("/profile")
    @CheckAuthenticatedUser
    public ResponseEntity<ProfileDto> getProfile(@RequestHeader("Authorization") String accessTokenHeader,
                                                              @AuthenticationPrincipal Member member) {
        ProfileDto profile = myAccountService.getProfile(member);
        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/update-gender")
    @CheckAuthenticatedUser
    public ResponseEntity<ApiResponse<String>> updateGender(@RequestHeader("Authorization") String accessTokenHeader,
                                                            @RequestBody ProfileChangeIntDto profileChangeDto,
                                                            @AuthenticationPrincipal Member member) {
        myAccountService.updateGender(member, profileChangeDto);
        return ApiResponse.success(HttpStatus.OK, "성별을 성공적으로 수정하였습니다.");
    }

    @PatchMapping("/update-age")
    @CheckAuthenticatedUser
    public ResponseEntity<ApiResponse<String>> updateAgeGroup(@RequestHeader("Authorization") String accessTokenHeader,
                                                            @RequestBody ProfileChangeIntDto profileChangeDto,
                                                            @AuthenticationPrincipal Member member) {
        myAccountService.updateAgeGroup(member, profileChangeDto);
        return ApiResponse.success(HttpStatus.OK, "연령대를 성공적으로 수정하였습니다.");
    }

    @PatchMapping("/update-nickname")
    @CheckAuthenticatedUser
    public ResponseEntity<ApiResponse<String>> updateNickname(@RequestHeader("Authorization") String accessTokenHeader,
                                                              @Valid @RequestBody NicknameDto nicknameDto,
                                                              @AuthenticationPrincipal Member member) {
        myAccountService.updateNickname(member,nicknameDto);
        return ApiResponse.success(HttpStatus.OK, "닉네임을 성공적으로 수정하였습니다.");
    }

    @PatchMapping("/update-constitution")
    @CheckAuthenticatedUser
    public ResponseEntity<ApiResponse<String>> updateConstitution(@RequestHeader("Authorization") String accessTokenHeader,
                                                              @Valid @RequestBody ProfileChangeIntDto profileChangeDto,
                                                              @AuthenticationPrincipal Member member) {
        myAccountService.updateConstitution(member, profileChangeDto);
        return ApiResponse.success(HttpStatus.OK, "체질을 성공적으로 수정하였습니다.");
    }

    @GetMapping("/location")
    @CheckAuthenticatedUser
    public ResponseEntity<ApiResponse<String>> getLocation(@RequestHeader("Authorization") String accessTokenHeader,
                                                           @AuthenticationPrincipal Member member) {
        String location = myAccountService.getLocation(member);
        return ApiResponse.success(HttpStatus.OK, location);
    }

    @PatchMapping("/update-location")
    @CheckAuthenticatedUser
    public ResponseEntity<ApiResponse<String>> updateLocation(
            @RequestHeader("Authorization") String accessTokenHeader,
            @Valid @RequestBody RegionDto regionDto,
            @AuthenticationPrincipal Member member) {

        try {
            myAccountService.updateLocation(member, regionDto);
            return ApiResponse.success(HttpStatus.OK, "지역을 성공적으로 수정하였습니다.");
        } catch (Exception e) {
            log.error("[지역 수정 실패] memberId={}, message={}", member != null ? member.getId() : null, e.getMessage(), e);
            return ApiResponse.success(HttpStatus.OK, "지역 수정 중 문제가 발생했습니다. 기존 데이터를 유지합니다.");
        }
    }

}
