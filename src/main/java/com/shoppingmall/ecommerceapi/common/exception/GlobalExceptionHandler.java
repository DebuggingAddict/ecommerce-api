package com.shoppingmall.ecommerceapi.common.exception;

import com.shoppingmall.ecommerceapi.common.api.Api;
import com.shoppingmall.ecommerceapi.common.code.ApiCode;
import com.shoppingmall.ecommerceapi.common.code.CommonErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<Api<Object>> handleBusiness(BusinessException e) {
    ApiCode code = e.getCode();

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

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<Api<Object>> handleMissingRequestHeader(MissingRequestHeaderException e) {
    ApiCode code = CommonErrorCode.MISSING_REQUIRED_HEADER;

    String description = "필수 헤더(" + e.getHeaderName() + ")가 누락되었습니다.";

    return ResponseEntity
        .status(code.getHttpStatus())
        .body(Api.ERROR(code, description));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Api<Object>> handleHttpMessageNotReadable(
      HttpMessageNotReadableException e) {
    ApiCode code = CommonErrorCode.BAD_REQUEST;

    String description = "요청 본문의 형식이 잘못되었거나 유효하지 않은 값이 포함되어 있습니다. (Enum 타입 확인)";

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(Api.ERROR(code, description));
  }
}