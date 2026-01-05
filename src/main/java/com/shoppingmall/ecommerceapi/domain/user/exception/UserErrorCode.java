package com.shoppingmall.ecommerceapi.domain.user.exception;

import com.shoppingmall.ecommerceapi.common.code.ApiCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * User 도메인 전용 에러 코드
 */
@AllArgsConstructor
@Getter
public enum UserErrorCode implements ApiCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 404, "사용자를 찾을 수 없습니다"),
    USER_NOT_ACTIVE(HttpStatus.BAD_REQUEST.value(), 400, "활성화되지 않은 사용자입니다"),
    USER_ALREADY_DEACTIVATED(HttpStatus.BAD_REQUEST.value(), 400, "이미 탈퇴한 계정입니다"),
    USER_ALREADY_ACTIVATED(HttpStatus.BAD_REQUEST.value(), 400, "이미 활성화된 계정입니다"),
    INVALID_GRADE(HttpStatus.BAD_REQUEST.value(), 400, "유효하지 않은 등급입니다. BASIC 또는 VIP만 가능합니다"),
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED.value(), 401, "인증되지 않은 사용자입니다");

    private final Integer httpStatus;
    private final Integer code;
    private final String message;
}

