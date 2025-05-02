package com.howWeather.howWeather_backend.domain.closet.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "outer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Outer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="outer_id")
    private Long id;

    private String outerName;

    private int color;

    private int thickness;
}
