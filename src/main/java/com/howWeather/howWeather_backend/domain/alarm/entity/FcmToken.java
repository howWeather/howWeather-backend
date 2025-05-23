package com.howWeather.howWeather_backend.domain.alarm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "fcm_token", uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "token"}))
public class FcmToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long memberId;
    private String token;
}