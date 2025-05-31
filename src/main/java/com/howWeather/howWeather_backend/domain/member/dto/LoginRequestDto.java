package com.howWeather.howWeather_backend.domain.member.dto;

import com.howWeather.howWeather_backend.global.custom.InvalidPrefix;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {
    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    @Size(min = 6, max = 20, message = "아이디는 6~20자 이내여야 합니다.")
    @InvalidPrefix(prefixes = { "Google_", "Kakao_" }, message = "아이디는 'Google_', 'Kakao_'로 시작할 수 없습니다.")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해야 합니다.")
    private String password;
}
