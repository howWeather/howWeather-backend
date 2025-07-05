package com.howWeather.howWeather_backend.domain.location.api;

import com.howWeather.howWeather_backend.domain.location.dto.LocationWeatherRequestDto;
import com.howWeather.howWeather_backend.domain.location.dto.RegionTemperatureDto;
import com.howWeather.howWeather_backend.domain.location.service.LocationService;
import com.howWeather.howWeather_backend.global.Response.ApiResponse;
import com.howWeather.howWeather_backend.global.jwt.CheckAuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;
    @GetMapping("")
    @CheckAuthenticatedUser
    public ResponseEntity<ApiResponse<String>> getRegionFromCoords(@RequestHeader("Authorization") String accessTokenHeader,
                                                                          @RequestBody @Valid LocationWeatherRequestDto request) {
        String region = locationService.reverseGeocode(request.getLatitude(), request.getLongitude());
        return ApiResponse.success(HttpStatus.OK, region);
    }

    @GetMapping("/temperature")
    @CheckAuthenticatedUser
    public ResponseEntity<ApiResponse<RegionTemperatureDto>> getRegionTemperature(@RequestHeader("Authorization") String accessTokenHeader,
                                                                                  @RequestBody @Valid LocationWeatherRequestDto request) {
        RegionTemperatureDto dto = locationService.getRegionTemperatureFromCoords(
                request.getLatitude(), request.getLongitude(), request.getTimeSlot(), request.getDate());
        return ApiResponse.success(HttpStatus.OK, dto);
    }
}
