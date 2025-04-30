package com.howWeather.howWeather_backend.global.exception;

public class LoginException extends RuntimeException {
    private final String errorCode;

    public LoginException(String message) {
        super(message);
        this.errorCode = "LOGIN_ERROR";
    }

    public LoginException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
