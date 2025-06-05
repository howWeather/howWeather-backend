package com.howWeather.howWeather_backend.domain.ai_model.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Objects;

@Data
@Builder
public class ClothingCombinationDto {
    private List<Integer> top;
    private List<Integer> outer;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClothingCombinationDto)) return false;
        ClothingCombinationDto that = (ClothingCombinationDto) o;
        return Objects.equals(top, that.top) && Objects.equals(outer, that.outer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(top, outer);
    }
}
