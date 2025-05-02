package com.howWeather.howWeather_backend.global.exception;

public record ExceptionDto(String code, String message) {
    public static ExceptionDto of(ErrorCode errorCode) {
        return new ExceptionDto(errorCode.name(), errorCode.getMessage());
    }
}
