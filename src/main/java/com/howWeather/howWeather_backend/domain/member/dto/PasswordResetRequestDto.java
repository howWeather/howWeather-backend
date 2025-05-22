package com.howWeather.howWeather_backend.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetRequestDto {
    @NotBlank(message = "아이디 또는 이메일을 입력해야 합니다.")
    private String identifier;
}
