package com.shoppingmall.ecommerceapi.common.exception;

import com.shoppingmall.ecommerceapi.common.code.ApiCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

  private final ApiCode code;
  private final String description; // 선택(없으면 null)

  public BusinessException(ApiCode code) {
    super(code.getMessage());
    this.code = code;
    this.description = null;
  }

  public BusinessException(ApiCode code, String description) {
    super(code.getMessage());
    this.code = code;
    this.description = description;
  }
}