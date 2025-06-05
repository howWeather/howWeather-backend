package com.howWeather.howWeather_backend.domain.closet.repository;

import com.howWeather.howWeather_backend.domain.closet.entity.Cloth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


@Repository
public interface ClothRepository extends JpaRepository<Cloth, Long> {
    @Query("SELECT c.url FROM Cloth c WHERE c.category = :category AND c.clothType = :clothType")
    Optional<String> findUrlByCategoryAndClothType(@Param("category") int category,
                                                   @Param("clothType") int clothType);

    @Query("SELECT c.thin FROM Cloth c WHERE c.category = :category AND c.clothType = :clothType")
    Optional<Integer> findThinByCategoryAndClothType(@Param("category") int category,
                                                     @Param("clothType") int clothType);

    @Query("SELECT c.normal FROM Cloth c WHERE c.category = :category AND c.clothType = :clothType")
    Optional<Integer> findNormalByCategoryAndClothType(@Param("category") int category,
                                                     @Param("clothType") int clothType);

    @Query("SELECT c.thick FROM Cloth c WHERE c.category = :category AND c.clothType = :clothType")
    Optional<Integer> findThickByCategoryAndClothType(@Param("category") int category,
                                                       @Param("clothType") int clothType);

    @Query("""
    SELECT c.clothType FROM Cloth c
    WHERE c.category = :category AND (c.thin = :heat OR c.normal = :heat OR c.thick = :heat)""")
    List<Long> findClothTypeByCategoryAndHeat(@Param("category") int category,
                                              @Param("heat") int heat);
}
