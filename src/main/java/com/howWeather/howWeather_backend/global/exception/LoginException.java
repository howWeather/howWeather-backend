package com.howWeather.howWeather_backend.global.exception;

public class LoginException extends CustomException {
    public LoginException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
