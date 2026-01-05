package com.shoppingmall.ecommerceapi.domain.auth.exception;

import com.shoppingmall.ecommerceapi.common.code.ApiCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ApiCode {

    // OAuth2 관련
    UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST.value(), 40001, "지원하지 않는 OAuth2 제공자입니다."),
    OAUTH_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED.value(), 40101, "OAuth2 인증에 실패했습니다."),

    // JWT 토큰 관련
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED.value(), 40102, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED.value(), 40103, "만료된 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED.value(), 40104, "유효하지 않은 Refresh Token입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 40401, "Refresh Token을 찾을 수 없습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED.value(), 40105, "만료된 Refresh Token입니다."),

    // 인증/인가 관련
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED.value(), 40106, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN.value(), 40301, "접근 권한이 없습니다."),

    // 사용자 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 40402, "사용자를 찾을 수 없습니다."),
    ;

    private final Integer httpStatus;
    private final Integer code;
    private final String message;
}
