package com.howWeather.howWeather_backend.global.exception;

import lombok.Getter;

public class LoginException extends CustomException {
    public LoginException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
