package com.howWeather.howWeather_backend.domain.closet.repository;

import com.howWeather.howWeather_backend.domain.closet.entity.Cloth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


@Repository
public interface ClothRepository extends JpaRepository<Cloth, Long> {
    @Query("SELECT c.url FROM Cloth c WHERE c.category = :category AND c.clothType = :clothType")
    Optional<String> findUrlByCategoryAndClothType(@Param("category") int category,
                                                   @Param("clothType") int clothType);
}
