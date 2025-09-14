package com.howWeather.howWeather_backend.domain.ai_model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ClothingRecommendation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private String regionName;

    @ElementCollection
    private List<Integer> tops;

    @ElementCollection
    private List<Integer> outers;

    @ElementCollection
    @CollectionTable(name = "recommendation_result", joinColumns = @JoinColumn(name = "recommendation_id"))
    @MapKeyColumn(name = "time")
    @Column(name = "prediction")
    private Map<String, Integer> predictionMap;

    private LocalDate date;
}
