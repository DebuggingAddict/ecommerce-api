package com.shoppingmall.ecommerceapi.common.exception;

import com.shoppingmall.ecommerceapi.common.api.Api;
import com.shoppingmall.ecommerceapi.common.code.ApiCode;
import com.shoppingmall.ecommerceapi.common.code.CommonErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<Api<Object>> handleBusiness(BusinessException e) {
    ApiCode code = e.getCode();

    // description이 없으면 기본 메시지로 내려도 됨(팀 룰에 맞게)
    String description = (e.getDescription() != null) ? e.getDescription() : code.getMessage();

    return ResponseEntity
        .status(code.getHttpStatus())
        .body(Api.ERROR(code, description));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Api<Object>> handleUnexpected(Exception e) {
    ApiCode code = CommonErrorCode.SERVER_ERROR;

    return ResponseEntity
        .status(code.getHttpStatus())
        .body(Api.ERROR(code, "예상치 못한 서버 오류가 발생했습니다."));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Api<Object>> handleValidationException(MethodArgumentNotValidException e) {
    String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();

    return ResponseEntity
        .status(CommonErrorCode.BAD_REQUEST.getHttpStatus())
        .body(Api.ERROR(CommonErrorCode.BAD_REQUEST, errorMessage));
  }
}