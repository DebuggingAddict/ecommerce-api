package com.shoppingmall.ecommerceapi.common.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum CommonErrorCode implements ApiCode {

  BAD_REQUEST(HttpStatus.BAD_REQUEST.value(), 400, "잘못된 요청"),
  SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), 500, "서버 에러");

  private final Integer httpStatus;
  private final Integer code;
  private final String message;
}
