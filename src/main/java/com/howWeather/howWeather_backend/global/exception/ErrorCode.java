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
    UNKNOWN_ERROR("서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_TOKEN("유효하지 않은 토큰입니다.", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED("토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_REISSUE_FAILED("토큰 재발급에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    LOGIN_ID_ALREADY_EXISTS("이미 사용 중인 아이디입니다.", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS("이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),
    TOKEN_REVOKED("해당 토큰은 로그아웃되어 사용할 수 없습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_ACCESS_TOKEN("유효하지 않은 Access Token입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN("유효하지 않은 Refresh Token입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_INPUT("입력 항목이 조건을 충족하지 않습니다", HttpStatus.BAD_REQUEST),
    CLOSET_NOT_FOUND("사용자의 옷장이 존재하지 않습니다. 옷장을 만들어주세요.", HttpStatus.NOT_FOUND),
    INVALID_CLOTH_REQUEST("등록할 의상 정보가 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_CLOTH_NAME("의상명이 잘못되었습니다", HttpStatus.BAD_REQUEST),
    INVALID_COLOR("색상값이 잘못되었습니다.", HttpStatus.BAD_REQUEST),
    INVALID_THICKNESS("두께값이 잘못되었습니다.", HttpStatus.BAD_REQUEST),
    INVALID_CLOTH_ID("의상 유형이 잘못되었습니다.", HttpStatus.BAD_REQUEST),
    CLOTH_NOT_FOUND("해당 의상을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    WRONG_PASSWORD("비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    PASSWORD_MISMATCH("변경할 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    SAME_PASSWORD("변경할 비밀번호는 기존의 비밀번호와 달라야 합니다.", HttpStatus.BAD_REQUEST);


    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
