package com.howWeather.howWeather_backend.domain.member.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequestDto {

    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    @Size(min = 6, max = 20, message = "아이디는 6~20자 이내여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]*$", message = "아이디는 영문, 숫자, 특수문자만 포함할 수 있습니다.")
    private String loginId;

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(min = 2, max = 10, message = "닉네임은 2~10자 이내여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]*$", message = "닉네임은 한글, 영문, 숫자, 특수문자만 포함할 수 있습니다.")
    private String nickname;

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식을 입력해주세요.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해야 합니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]{8,20}$", message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String password;

    @NotNull(message = "체질은 필수 입력 항목입니다.")
    @Min(value = 1, message = "선택한 체질 정보가 유효하지 않습니다.")
    @Max(value = 3, message = "선택한 체질 정보가 유효하지 않습니다.")
    private int constitution;

    @NotNull(message = "체형은 필수 입력 항목입니다.")
    @Min(value = 1, message = "선택한 체형 정보가 유효하지 않습니다.")
    @Max(value = 3, message = "선택한 체형 정보가 유효하지 않습니다.")
    private int bodyType;

    @NotNull(message = "성별은 필수 입력 항목입니다.")
    @Min(value = 1, message = "선택한 성별 정보가 유효하지 않습니다.")
    @Max(value = 2, message = "선택한 성별 정보가 유효하지 않습니다.")
    private int gender;

    @NotNull(message = "연령대는 필수 입력 항목입니다.")
    @Min(value = 1, message = "선택한 연령대 정보가 유효하지 않습니다.")
    @Max(value = 3, message = "선택한 연령대 정보가 유효하지 않습니다.")
    private int ageGroup;
}
