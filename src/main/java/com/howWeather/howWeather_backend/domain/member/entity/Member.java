package com.howWeather.howWeather_backend.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "member")
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder @AllArgsConstructor
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="member_id")
    private Long id;

    @Column(name="member_login_id", unique = true, nullable = false)
    private String loginId;

    @Column(name="member_pw", nullable = false)
    private String password;

    @Column(name="member_email", unique = true, nullable = false)
    private String email;

    @Column(name="member_nickname", nullable = false, length = 100)
    private String nickname;

    @Column(name="member_constitution", nullable = false)
    private int constitution;

    @Column(name="age_group", nullable = false)
    private int ageGroup;

    @Column(name="body_type", nullable = false)
    private int bodyType;

    @Column(name="gender", nullable = false)
    private int gender;

    @Column(name="sensitivity", nullable = false)
    private int sensitivity = -1; // init
}
