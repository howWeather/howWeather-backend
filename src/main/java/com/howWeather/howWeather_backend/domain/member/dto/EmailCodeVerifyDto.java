package com.howWeather.howWeather_backend.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailCodeVerifyDto {
    @NotBlank @Email
    private String email;

    @NotBlank
    private String code;
}
