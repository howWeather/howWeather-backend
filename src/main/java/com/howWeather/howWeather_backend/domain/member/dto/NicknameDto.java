package com.howWeather.howWeather_backend.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NicknameDto {
    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(min = 2, max = 10, message = "닉네임은 2~10자 이내여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]*$", message = "닉네임은 한글, 영문, 숫자, 특수문자만 포함할 수 있습니다.")
    private String data;
}
