package com.howWeather.howWeather_backend.domain.closet.dto;

import com.howWeather.howWeather_backend.global.custom.ValidRange;
import lombok.Data;

@Data
public class UpdateClothDto {

    @ValidRange(min = 1, max = 11, message = "지원하지 않는 색상값입니다.")
    private Integer color;

    @ValidRange(min = 1, max = 3, message = "지원하지 않는 두께값입니다.")
    private Integer thickness;
}
