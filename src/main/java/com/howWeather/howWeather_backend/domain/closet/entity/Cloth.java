package com.howWeather.howWeather_backend.domain.closet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cloth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "cloth_type", nullable = false)
    private int clothType;

    @Column(nullable = false)
    private int thin;

    @Column(nullable = false)
    private int  normal;

    @Column(nullable = false)
    private int thick;

    @Column(length = 1024, nullable = false)
    private String url;

    @Column(nullable = false)
    private int category;
}
