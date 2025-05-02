package com.howWeather.howWeather_backend.domain.closet.entity;

import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.FetchType.*;

@Entity
@Table(name = "upper_wear")
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

    private boolean isActive;

    @Setter
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "closet_id")
    private Closet closet;

    public static boolean isDuplicate(Upper existingUpper, Upper newUpper) {
        return existingUpper.getUpperName().equals(newUpper.getUpperName()) &&
                existingUpper.getColor() == newUpper.getColor() &&
                existingUpper.getThickness() == newUpper.getThickness();
    }
}
