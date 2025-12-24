package com.shoppingmall.ecommerceapi.domain.product.exception;

import com.shoppingmall.ecommerceapi.common.code.ApiCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ProductErrorCode implements ApiCode {
  // 400 Bad Request: 입력값 및 비즈니스 검증 실패
  PRODUCT_INVALID_NAME(HttpStatus.BAD_REQUEST.value(), 400, "상품 이름 형식 오류 (2~50자 미준수, 특수문자/공백 포함)"),
  PRODUCT_INVALID_DESCRIPTION(HttpStatus.BAD_REQUEST.value(), 400,
      "상품 설명 길이 초과 (200자 초과) 또는 형식 오류"),
  PRODUCT_INVALID_PRICE(HttpStatus.BAD_REQUEST.value(), 400, "가격 범위 오류 (0원 미만 또는 50,000원 초과)"),
  PRODUCT_INVALID_CATEGORY(HttpStatus.BAD_REQUEST.value(), 400, "정의되지 않은 카테고리 값 입력"),
  PRODUCT_INVALID_STOCK(HttpStatus.BAD_REQUEST.value(), 400, "재고 수량 범위 오류 (0 미만 또는 10,000 초과)"),
  PRODUCT_INVALID_IMAGE(HttpStatus.BAD_REQUEST.value(), 400, "지원하지 않는 이미지 확장자이거나 잘못된 이미지 URL 형식"),

  // 401 Unauthorized: 인증 실패
  PRODUCT_UNAUTHENTICATED(HttpStatus.UNAUTHORIZED.value(), 401, "상품을 등록하려면 관리자 계정으로 로그인이 필요합니다."),

  // 403 Forbidden: 권한 부족
  PRODUCT_FORBIDDEN(HttpStatus.FORBIDDEN.value(), 403, "상품 등록 권한이 없는 유저가 관리자 기능을 시도했습니다.");

  private final Integer httpStatus;
  private final Integer code;
  private final String message;

}
