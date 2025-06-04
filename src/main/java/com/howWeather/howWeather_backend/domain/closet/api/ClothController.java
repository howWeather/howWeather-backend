package com.howWeather.howWeather_backend.domain.closet.api;

import com.howWeather.howWeather_backend.domain.closet.service.ClothService;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.global.Response.ApiResponse;
import com.howWeather.howWeather_backend.global.jwt.CheckAuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cloth")
public class ClothController {
    private final ClothService clothService;

    @GetMapping("/upper-image/{clothType}")
    @CheckAuthenticatedUser
    public ResponseEntity<ApiResponse<String>> upperImage(@RequestHeader("Authorization") String accessTokenHeader,
                                                          @AuthenticationPrincipal Member member,
                                                          @PathVariable("clothType") int clothType) {
        String url = clothService.getUpperImage(clothType);
        return ApiResponse.success(HttpStatus.OK, url);
    }

    @GetMapping("/outer-image/{clothType}")
    @CheckAuthenticatedUser
    public ResponseEntity<ApiResponse<String>> outerImage(@RequestHeader("Authorization") String accessTokenHeader,
                                                          @AuthenticationPrincipal Member member,
                                                          @PathVariable("clothType") int clothType) {
        String url = clothService.getOuterImage(clothType);
        return ApiResponse.success(HttpStatus.OK, url);
    }
}
