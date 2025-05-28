package com.howWeather.howWeather_backend.domain.member.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KakaoUserInfo {
    private Long id;
    private KakaoAccount kakao_account;

    public String getEmail() {
        return kakao_account.getEmail();
    }

    public String getNickname() {
        return kakao_account.getProfile().getNickname();
    }

    @Data
    public static class KakaoAccount {
        private String email;
        private Profile profile;

        @Data
        public static class Profile {
            private String nickname;
        }
    }
}
