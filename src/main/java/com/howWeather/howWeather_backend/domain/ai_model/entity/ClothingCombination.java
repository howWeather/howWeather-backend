package com.howWeather.howWeather_backend.domain.ai_model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    private LocalDateTime lastModified;

    public void updateCombinations(String combinationsJson, LocalDateTime updatedDate) {
        this.combinationsJson = combinationsJson;
        this.lastModified = updatedDate;
    }
}