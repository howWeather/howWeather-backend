package com.howWeather.howWeather_backend.domain.member.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileDto {
    private String nickname;
    private String loginId;
    private String email;
    private int ageGroup;
    private int bodyType;
    private int gender;
}