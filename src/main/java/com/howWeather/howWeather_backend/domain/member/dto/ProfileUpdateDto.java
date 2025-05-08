package com.howWeather.howWeather_backend.domain.member.dto;

import com.howWeather.howWeather_backend.global.custom.ValidRange;
import com.howWeather.howWeather_backend.global.custom.ValidString;
import lombok.Data;

@Data
public class ProfileUpdateDto {
    @ValidString(
            min = 2,
            max = 10,
            regexp = "^[a-zA-Z0-9가-힣!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]*$",
            message = "닉네임은 2~10자 이내의 한글, 영문, 숫자, 특수문자만 포함할 수 있습니다."
    )
    private String nickname;

    @ValidRange(min = 1, max = 3, message = "선택한 체형 정보가 유효하지 않습니다.")
    private Integer bodyType;

    @ValidRange(min = 1, max = 3, message = "선택한 연령대 정보가 유효하지 않습니다.")
    private Integer ageGroup;

}
