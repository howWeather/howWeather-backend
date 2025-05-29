package com.howWeather.howWeather_backend.domain.member.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class KakaoUserInfo {
    private Long id;
    private KakaoAccount kakao_account;

    public KakaoUserInfo(Map<String, Object> attributes) {
        this.id = ((Number) attributes.get("id")).longValue();

        Map<String, Object> accountMap = (Map<String, Object>) attributes.get("kakao_account");
        this.kakao_account = new KakaoAccount(accountMap);
    }

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

        public KakaoAccount(Map<String, Object> accountMap) {
            this.email = (String) accountMap.get("email");
            Map<String, Object> profileMap = (Map<String, Object>) accountMap.get("profile");
            this.profile = new Profile(profileMap);
        }

        @Data
        public static class Profile {
            private String nickname;

            public Profile(Map<String, Object> profileMap) {
                this.nickname = (String) profileMap.get("nickname");
            }
        }
    }
}
