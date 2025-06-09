package com.howWeather.howWeather_backend.domain.ai_model.dto;

import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import lombok.Data;

@Data
public class HistoryRequestDto {
    public static final int DEFAULT_CNT = 10;
    public static final double DEFAULT_GAP = 2.0;

    private Long memberId;

    private Double temperature;

    private Integer cnt;

    private Double upperGap;

    private Double lowerGap;

    public double getUpperBound() {
        return temperature + (upperGap != null ? upperGap : DEFAULT_GAP);
    }

    public double getLowerBound() {
        return temperature - (lowerGap != null ? lowerGap : DEFAULT_GAP);
    }

    public void validate() {
        if (memberId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "사용자 id는 필수입니다.");
        }
        if (temperature == null || temperature < -50 || temperature > 60) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        if (cnt != null && (cnt < 0 || cnt > 20)) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        if (upperGap != null && (upperGap < 0 || upperGap > 10)) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        if (lowerGap != null && (lowerGap < 0 || lowerGap > 10)) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
    }
}
