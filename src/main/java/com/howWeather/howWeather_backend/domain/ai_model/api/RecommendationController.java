package com.howWeather.howWeather_backend.domain.ai_model.api;

import com.howWeather.howWeather_backend.domain.ai_model.dto.RecommendPredictDto;
import com.howWeather.howWeather_backend.domain.ai_model.service.RecommendationService;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.global.Response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/model")
public class RecommendationController {
    private final RecommendationService recommendationService;

    @GetMapping("/recommendation")
    public ResponseEntity<ApiResponse<List<RecommendPredictDto>>> getRecommendList(@RequestHeader("Authorization") String accessTokenHeader,
                                                                                   @AuthenticationPrincipal Member member) {

        List<RecommendPredictDto> recommendList = recommendationService.getRecommendList(member);
        return ApiResponse.success(HttpStatus.OK, recommendList);
    }
}