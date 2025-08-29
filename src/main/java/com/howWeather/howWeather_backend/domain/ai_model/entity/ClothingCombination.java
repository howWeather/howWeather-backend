package com.howWeather.howWeather_backend.domain.ai_model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "clothing_combinations")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClothingCombination {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    @Lob
    private String combinationsJson;

    private LocalDate lastUpdated;

    public void updateCombinations(String combinationsJson, LocalDate updatedDate) {
        this.combinationsJson = combinationsJson;
        this.lastUpdated = updatedDate;
    }
}