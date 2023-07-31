package com.example.translationchat.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // signUp
    ALREADY_REGISTERED_EMAIL(HttpStatus.BAD_REQUEST, "이미 가입된 회원 입니다."),
    ALREADY_REGISTERED_NAME(HttpStatus.BAD_REQUEST, "이미 사용중인 이름입니다."),
    // login
    LOGIN_FAIL(HttpStatus.BAD_REQUEST, "로그인에 실패하였습니다."),

    NOT_FOUND_USER(HttpStatus.BAD_REQUEST, "회원을 찾을 수 없습니다."),
    LOGIN_REQUIRED(HttpStatus.BAD_REQUEST, "인증이 유효하지 않습니다. 다시 로그인 후 이용해주세요."),

    CAN_NOT_FRIEND_YOURSELF(HttpStatus.BAD_REQUEST, "사용자 자신을 친구 요청 할 수 없습니다."),
    ALREADY_REGISTERED_FRIENDSHIP(HttpStatus.BAD_REQUEST, "이미 친구로 등록 되어있습니다."),
    ALREADY_REQUEST_FRIENDSHIP(HttpStatus.BAD_REQUEST, "이미 친구 요청을 한 지 한달이 안됐습니다."),
    FRIENDSHIP_STATUS_IS_BLOCKED(HttpStatus.BAD_REQUEST, "차단된 친구입니다."),
    FRIENDSHIP_STATUS_IS_NOT_BLOCKED(HttpStatus.BAD_REQUEST, "차단된 친구가 아닙니다."),
    NOT_FOUND_FRIENDSHIP(HttpStatus.BAD_REQUEST, "친구 기록이 없습니다."),
    ALREADY_OPPONENT_REQUEST(HttpStatus.BAD_REQUEST, "이미 상대가 유저에게 친구 요청을 했습니다. 친구 요청 알림메세지를 확인해주세요."),

    NOT_FOUND_NOTIFICATION(HttpStatus.BAD_REQUEST, "해당 알림이 존재하지 않습니다."),

    LOCK_FAILED(HttpStatus.BAD_REQUEST, "이메일 또는 이름이 이미 사용중입니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "올바른 입력값이 아닙니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
