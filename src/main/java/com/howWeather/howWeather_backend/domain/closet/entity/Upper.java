package com.howWeather.howWeather_backend.domain.closet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.function.BiFunction;

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

    private Long upperType;

    private int color;

    private int thickness;

    private Integer warmthIndex;

    @Setter
    private boolean isActive;

    private boolean isLayerFlexible;

    public void patchAttributes(Integer color, Integer thickness,
                                BiFunction<Long, Integer, Integer> warmthCalculator) {
        if (color != null) {
            this.color = color;
        }
        if (thickness != null) {
            this.thickness = thickness;
            this.warmthIndex = warmthCalculator.apply(this.upperType, thickness);
        }
    }

    @Setter
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "closet_id")
    private Closet closet;

    public static boolean isDuplicate(Upper existingUpper, Upper newUpper) {
        return existingUpper.getUpperType() == newUpper.getUpperType() &&
                existingUpper.getColor() == newUpper.getColor() &&
                existingUpper.getThickness() == newUpper.getThickness();
    }
}
