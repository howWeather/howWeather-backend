package com.howWeather.howWeather_backend.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    USER_ALREADY_EXISTS("이미 존재하는 사용자입니다.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("인증에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND("아이디가 존재하지 않습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS("비밀번호가 틀렸습니다.", HttpStatus.UNAUTHORIZED),
    AUTHENTICATION_FAILED("인증에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    UNKNOWN_ERROR("서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);


    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

}
