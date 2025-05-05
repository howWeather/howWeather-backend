package com.howWeather.howWeather_backend.domain.closet.api;

import com.howWeather.howWeather_backend.domain.closet.dto.ClothListDto;
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
    ResponseEntity<ApiResponse<String>> registerCloth(@RequestHeader("Authorization") String accessTokenHeader,
                                                      @Valid @RequestBody AddClothesDto addClothesDto,
                                                      @AuthenticationPrincipal Member member) {
        closetService.registerCloset(member, addClothesDto);
        return ApiResponse.success(HttpStatus.OK, "의상 등록에 성공하였습니다.");
    }

    @GetMapping("/search-upper")
    public ResponseEntity<ClothListDto> getUpperClothesByName(@RequestHeader("Authorization") String accessTokenHeader,
                                                               @RequestParam String name,
                                                               @AuthenticationPrincipal Member member) {
        ClothListDto clothes = closetService.findUppersByName(member, name);
        return ResponseEntity.ok(clothes);
    }

    @GetMapping("/search-outer")
    public ResponseEntity<ClothListDto> getOuterClothesByName(@RequestHeader("Authorization") String accessTokenHeader,
                                                         @RequestParam String name,
                                                         @AuthenticationPrincipal Member member) {
        ClothListDto clothes = closetService.findOutersByName(member, name);
        return ResponseEntity.ok(clothes);
    }


}
