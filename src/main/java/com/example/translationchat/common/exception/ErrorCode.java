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

    CAN_NOT_FAVORITE_YOURSELF(HttpStatus.BAD_REQUEST, "사용자 자신을 즐겨찾기에 등록할 수 없습니다."),
    ALREADY_REGISTERED_FAVORITE(HttpStatus.BAD_REQUEST, "이미 즐겨찾기에 등록 되어있습니다."),
    NOT_REGISTERED_FAVORITE(HttpStatus.BAD_REQUEST, "즐겨찾기에 등록된 유저가 아닙니다."),
    USER_IS_BLOCKED(HttpStatus.BAD_REQUEST, "차단된 유저입니다."),
    USER_IS_NOT_BLOCKED(HttpStatus.BAD_REQUEST, "차단된 유저가 아닙니다."),

    OFFLINE_USER(HttpStatus.BAD_REQUEST, "유저가 오프라인 상태입니다."),
    ALREADY_REQUEST_RECEIVER(HttpStatus.BAD_REQUEST, "상대방이 이미 유저에게 대화 요청을 하였습니다."),
    ALREADY_REQUEST(HttpStatus.BAD_REQUEST, "이미 상대방에게 대화 요청을 하였습니다."),
    ALREADY_EXISTS_ROOM(HttpStatus.BAD_REQUEST, "이미 유저와의 대화방이 있습니다."),

    NOT_FOUND_NOTIFICATION(HttpStatus.BAD_REQUEST, "해당 알림이 존재하지 않습니다."),
    NOT_YOUR_NOTIFICATION(HttpStatus.BAD_REQUEST, "유저의 알림이 아닙니다."),

    NOT_EXIST_CLIENT(HttpStatus.BAD_REQUEST, "해당 채팅방에 클라이언트가 없습니다."),
    NOT_INVALID_ROOM(HttpStatus.BAD_REQUEST, "대화방이 유효하지 않습니다."),

    ALREADY_RANDOM_CHAT_ROOM(HttpStatus.BAD_REQUEST, "이미 랜덤 채팅방에 참여 중 있습니다."),
    ALREADY_RANDOM_CHAT_UNAVAILABLE(HttpStatus.BAD_REQUEST, "신고 대상이 이미 랜덤 채팅 이용 불가 상태입니다."),
    REPORTED_WITHIN_30_DAYS(HttpStatus.BAD_REQUEST, "이미 30일이내 신고한 적이 있습니다."),
    RANDOM_CHAT_UNAVAILABLE_STATUS(HttpStatus.BAD_REQUEST, "랜덤 채팅 이용 불가 상태입니다."),

    LOCK_FAILED(HttpStatus.BAD_REQUEST, "락 실패, 해당 키는 이미 사용중입니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "올바른 입력값이 아닙니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
