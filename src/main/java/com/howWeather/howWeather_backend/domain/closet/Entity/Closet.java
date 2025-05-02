package com.howWeather.howWeather_backend.domain.closet.Entity;

import com.howWeather.howWeather_backend.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "closet")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Closet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="closet_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;


}
