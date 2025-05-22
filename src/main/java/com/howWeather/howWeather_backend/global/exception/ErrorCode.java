package com.howWeather.howWeather_backend.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 공통
    UNAUTHORIZED("인증에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    AUTHENTICATION_FAILED("인증에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    UNKNOWN_ERROR("서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_INPUT("입력 항목이 조건을 충족하지 않습니다", HttpStatus.BAD_REQUEST),
    
    // 회원가입/로그인/로그아웃/비밀번호 설정 관련
    USER_ALREADY_EXISTS("이미 존재하는 사용자입니다.", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("아이디가 존재하지 않습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS("비밀번호가 틀렸습니다.", HttpStatus.UNAUTHORIZED),
    LOGIN_ID_ALREADY_EXISTS("이미 사용 중인 아이디입니다.", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS("이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),
    WRONG_PASSWORD("비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    PASSWORD_MISMATCH("변경할 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    SAME_PASSWORD("변경할 비밀번호는 기존의 비밀번호와 달라야 합니다.", HttpStatus.BAD_REQUEST),
    ALREADY_DELETED("탈퇴한 계정입니다.", HttpStatus.BAD_REQUEST),
    EMAIL_SEND_FAIL("임시 비밀번호 이메일 전송에 실패했습니다.", HttpStatus.SERVICE_UNAVAILABLE),

    // 토큰
    INVALID_TOKEN("유효하지 않은 토큰입니다.", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED("토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_REISSUE_FAILED("토큰 재발급에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_REVOKED("해당 토큰은 로그아웃되어 사용할 수 없습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_ACCESS_TOKEN("유효하지 않은 Access Token입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN("유효하지 않은 Refresh Token입니다.", HttpStatus.UNAUTHORIZED),

    // 옷장
    CLOSET_NOT_FOUND("사용자의 옷장이 존재하지 않습니다. 옷장을 만들어주세요.", HttpStatus.NOT_FOUND),
    INVALID_CLOTH_REQUEST("등록할 의상 정보가 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_CLOTH_NAME("의상명이 잘못되었습니다", HttpStatus.BAD_REQUEST),
    INVALID_COLOR("색상값이 잘못되었습니다.", HttpStatus.BAD_REQUEST),
    INVALID_THICKNESS("두께값이 잘못되었습니다.", HttpStatus.BAD_REQUEST),
    INVALID_CLOTH_ID("의상 유형이 잘못되었습니다.", HttpStatus.BAD_REQUEST),
    CLOTH_NOT_FOUND("해당 의상을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    DUPLICATE_RECORD("이미 해당 날짜와 시간대의 기록이 존재합니다", HttpStatus.CONFLICT),
    
    // 기록 달력
    UNABLE_RECORD_TIME("아직 해당 시간대 기록을 작성할 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_TIMESLOT("오전/오후/저녁에 해당하는 시간대만 작성할 수 있습니다.", HttpStatus.BAD_REQUEST),
    TOO_LATE_TO_RECORD("전날의 기록은 오늘 새벽 5시 30분 전까지만 작성할 수 있습니다.", HttpStatus.BAD_REQUEST),
    INVALID_DATE("해당 시간대의 기록을 작성할 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_DATE_REQUEST("세부 날짜의 데이터를 조회하기 위해서는 YYYY-MM-DD 형태를 사용해야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_MONTH_REQUEST("한 달의 데이터를 조회하기 위해서는 YYYY-MM 형태를 사용해야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_TEMP_GAP("온도 범위는 0 ~ 10도 사이의 값으로 설정 가능합니다.", HttpStatus.BAD_REQUEST),
    INVALID_TEMP("현재 온도는 섭씨로 -50 이상 60 이하의 값이어야 합니다.", HttpStatus.BAD_REQUEST),

    // 닐씨
    API_CALL_ERROR("날씨 API를 호출하는 중 오류가 발생했습니다", HttpStatus.SERVICE_UNAVAILABLE),
    NO_BODY_ERROR("날씨 데이터가 존재하지 않습니다", HttpStatus.NOT_FOUND),
    REGION_NOT_FOUND("해당 지역은 서비스가 제공되지 않습니다.", HttpStatus.NOT_FOUND);


    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
