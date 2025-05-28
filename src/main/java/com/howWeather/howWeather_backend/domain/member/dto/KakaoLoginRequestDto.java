package com.howWeather.howWeather_backend.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoLoginRequestDto {
    @NotBlank
    private String kakaoAccessToken;
}
