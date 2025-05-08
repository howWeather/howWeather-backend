package com.howWeather.howWeather_backend.domain.closet.entity;

import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.FetchType.*;

@Entity
@Table(name = "outer_wear")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Outer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="outer_id")
    private Long id;

    private Long outerType;

    private int color;

    private int thickness;

    private boolean isActive;

    private boolean isLayerFlexible;

    public void patchAttributes(Integer color, Integer thickness) {
        if (color != null) {
            this.color = color;
        }
        if (thickness != null) {
            this.thickness = thickness;
        }
    }

    @Setter
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "closet_id")
    private Closet closet;

    public static boolean isDuplicate(Outer existingOuter, Outer newOuter) {
        return existingOuter.getOuterType() == newOuter.getOuterType() &&
                existingOuter.getColor() == newOuter.getColor() &&
                existingOuter.getThickness() == newOuter.getThickness();
    }
}