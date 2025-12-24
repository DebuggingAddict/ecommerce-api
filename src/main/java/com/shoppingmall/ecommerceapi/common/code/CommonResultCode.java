package com.shoppingmall.ecommerceapi.common.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CommonResultCode implements ApiCode {

  OK(200, 200, "성공"),
  CREATED(201, 201, "생성 성공");

  private final Integer httpStatus;
  private final Integer code;
  private final String message;
}
