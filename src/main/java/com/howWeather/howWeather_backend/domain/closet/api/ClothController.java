package com.howWeather.howWeather_backend.domain.closet.api;

import com.howWeather.howWeather_backend.domain.closet.service.ClothService;
import com.howWeather.howWeather_backend.global.Response.ApiResponse;
import com.howWeather.howWeather_backend.global.jwt.CheckAuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cloth")
public class ClothController {
    private final ClothService clothService;

    @GetMapping("/upper-image/{clothId}")
    @CheckAuthenticatedUser
    public ResponseEntity<ApiResponse<String>> upperImage(@PathVariable("clothId") int clothId) {
        String url = clothService.getUpperImage(clothId);
        return ApiResponse.success(HttpStatus.OK, url);
    }
}
