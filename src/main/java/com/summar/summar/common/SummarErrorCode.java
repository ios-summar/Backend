package com.summar.summar.common;

public enum SummarErrorCode {
    // common
    COMMON_FAIL(0, "E000", "실패"),
    METHOD_NOT_ALLOWED(1, "E010", "허용되지 않은 Method로 접근 하셨습니다."),
    FORBIDDEN(2, "E011", "접근 권한이 없습니다."),


    // token
    INVALID_TOKEN(100, "E001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(101, "E002", "만료된 토큰입니다."),
    AUTHENTICATION_FAILED(102, "E003", "인증에 실패하였습니다."),
    WRONG_TOKEN(103, "E004", "잘못된 토큰입니다."),

    // user
    USER_NOT_FOUND(200, "E100", "존재하지 않는 사용자입니다."),
    INVALID_USER_INFO(201, "E101", "유효하지 않은 사용자 정보입니다."),
    UNAUTHORIZED(202, "E102", "승인된 계정이 아닙니다."),
    PASSWORD_NOT_MATCH(203, "E103", "비밀번호가 일치하지 않습니다."),
    LOCKED_USER(204, "E104", "보안상의 이유로 잠긴 상태의 계정입니다."),
    EXPIRED_USER(205, "E105", "사용 기간이 만료된 계정입니다."),
    EXPIRED_PASSWORD(206, "E106", "비밀번호가 만료되었습니다."),
    USER_ALREADY_EXIST(207, "E107", "이미 존재하는 사용자입니다."),
    USER_IS_NULL(200, "E108", "회원 정보가 필요합니다."),
    DUPLICATED_NICKNAME(202, "E109", "이미 사용 중인 닉네임입니다."),
    DUPLICATED_USERID(202, "E110", "이미 사용 중인 아이디입니다."),
    ;

    private final String code;
    private final String message;
    private int status;

    SummarErrorCode(final int status, final String code, final String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
