package com.howWeather.howWeather_backend.global.jwt;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@NoArgsConstructor
public class RefreshToken {
    @Id
    private String username;
    private String refreshToken;
}
