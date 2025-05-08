package com.howWeather.howWeather_backend.domain.closet.api;

import com.howWeather.howWeather_backend.domain.closet.dto.ClothListDto;
import com.howWeather.howWeather_backend.domain.closet.dto.UpdateClothDto;
import com.howWeather.howWeather_backend.domain.closet.service.ClosetService;
import com.howWeather.howWeather_backend.domain.closet.dto.AddClothesDto;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.global.Response.ApiResponse;
import com.howWeather.howWeather_backend.global.jwt.CheckAuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/closet")
public class ClosetController {
    private final ClosetService closetService;

    @PostMapping("/register")
    @CheckAuthenticatedUser
    public ResponseEntity<ApiResponse<String>> registerCloth(
            @RequestHeader("Authorization") String accessTokenHeader,
            @Valid @RequestBody AddClothesDto addClothesDto,
            @AuthenticationPrincipal Member member) {
        closetService.registerCloset(member, addClothesDto);
        return ApiResponse.success(HttpStatus.OK, "의상 등록에 성공하였습니다.");
    }

    @GetMapping("/uppers")
    @CheckAuthenticatedUser
    public ResponseEntity<List<ClothListDto>> getUpperClothesByName(
            @RequestHeader("Authorization") String accessTokenHeader,
            @AuthenticationPrincipal Member member) {

        List<ClothListDto> clothes = closetService.findActiveUppers(member);
        return ResponseEntity.ok(clothes);
    }

    @GetMapping("/outers")
    @CheckAuthenticatedUser
    public ResponseEntity<List<ClothListDto>> getOuterClothesByName(
            @RequestHeader("Authorization") String accessTokenHeader,
            @AuthenticationPrincipal Member member) {
        List<ClothListDto> clothes = closetService.findActiveOuters(member);
        return ResponseEntity.ok(clothes);
    }

    @PatchMapping("/update-upper/{clothId}")
    @CheckAuthenticatedUser
    public ResponseEntity<ApiResponse<String>> updateUpper(
            @RequestHeader("Authorization") String accessTokenHeader,
            @PathVariable Long clothId,
            @Valid @RequestBody UpdateClothDto updateDto,
            @AuthenticationPrincipal Member member) {

        closetService.updateUpper(clothId, updateDto, member);
        return ApiResponse.success(HttpStatus.OK, "상의를 성공적으로 수정하였습니다.");
    }

    @PatchMapping("/update-outer/{clothId}")
    @CheckAuthenticatedUser
    public ResponseEntity<ApiResponse<String>> updateOuter(
            @RequestHeader("Authorization") String accessTokenHeader,
            @PathVariable Long clothId,
            @Valid @RequestBody UpdateClothDto updateDto,
            @AuthenticationPrincipal Member member) {

        closetService.updateOuter(clothId, updateDto, member);
        return ApiResponse.success(HttpStatus.OK, "아우터를 성공적으로 수정하였습니다.");
    }

    @DeleteMapping("/delete-upper/{clothId}")
    @CheckAuthenticatedUser
    public ResponseEntity<ApiResponse<String>> deleteUpper(
            @RequestHeader("Authorization") String accessTokenHeader,
            @PathVariable Long clothId,
            @AuthenticationPrincipal Member member) {
        closetService.deleteUpper(clothId, member);
        return ApiResponse.success(HttpStatus.NO_CONTENT, "상의를 성공적으로 삭제하였습니다.");
    }

    @DeleteMapping("/delete-outer/{clothId}")
    @CheckAuthenticatedUser
    public ResponseEntity<ApiResponse<String>> deleteOuter(
            @RequestHeader("Authorization") String accessTokenHeader,
            @PathVariable Long clothId,
            @AuthenticationPrincipal Member member) {
        closetService.deleteOuter(clothId, member);
        return ApiResponse.success(HttpStatus.NO_CONTENT, "아우터를 성공적으로 삭제하였습니다.");
    }
}
