package com.howWeather.howWeather_backend.member.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "member")
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder @AllArgsConstructor
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="member_id")
    private Long id;

    @Column(name="member_login_id", unique = true)
    private String loginId;

    @Column(name="member_pw")
    private String password;

    @Column(name="member_email", unique = true)
    private String email;

    @Column(name="member_nickname", nullable = false, length = 100)
    private String nickname;

    @Column(name="member_constitution", nullable = false)
    private int constitution;

}
