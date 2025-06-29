package com.howWeather.howWeather_backend.domain.location.api;

import com.howWeather.howWeather_backend.domain.location.dto.LocationWeatherRequestDto;
import com.howWeather.howWeather_backend.domain.location.dto.RegionInfoDto;
import com.howWeather.howWeather_backend.domain.location.dto.RegionTemperatureDto;
import com.howWeather.howWeather_backend.domain.location.service.LocationService;
import com.howWeather.howWeather_backend.global.Response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;
    @PostMapping("/region")
    public ResponseEntity<ApiResponse<RegionInfoDto>> getRegionFromCoords(@RequestHeader("Authorization") String accessTokenHeader,
                                                                          @RequestBody @Valid LocationWeatherRequestDto request) {
        RegionInfoDto region = locationService.reverseGeocode(request.getLatitude(), request.getLongitude());
        return ApiResponse.success(HttpStatus.OK, region);
    }

    @PostMapping("/region/temperature")
    public ResponseEntity<ApiResponse<RegionTemperatureDto>> getRegionTemperature(@RequestHeader("Authorization") String accessTokenHeader,
                                                                                  @RequestBody @Valid LocationWeatherRequestDto request) {
        RegionTemperatureDto dto = locationService.getRegionTemperatureFromCoords(
                request.getLatitude(), request.getLongitude(), request.getTimeSlot(), request.getDate());
        return ApiResponse.success(HttpStatus.OK, dto);
    }
}
