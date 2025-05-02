package com.howWeather.howWeather_backend.domain.closet.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "upper")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Upper {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="upper_id")
    private Long id;

    private String upperName;

    private int color;

    private int thickness;
}
