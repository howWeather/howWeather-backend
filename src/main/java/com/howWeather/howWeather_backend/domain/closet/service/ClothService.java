package com.howWeather.howWeather_backend.domain.closet.service;

import com.howWeather.howWeather_backend.domain.closet.repository.ClothRepository;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class ClothService {
    private final ClothRepository clothRepository;

    @Transactional(readOnly = true)
    public String getUpperImage(int id) {
        return clothRepository.findUrlByCategoryAndClothType(1, id)
                .orElseThrow(() -> new CustomException(ErrorCode.CLOTH_NOT_FOUND));
    }
}
