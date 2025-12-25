package com.shoppingmall.ecommerceapi.domain.product.exception;

import com.shoppingmall.ecommerceapi.common.code.ApiCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ProductErrorCode implements ApiCode {
  // 상품 등록
  PRODUCT_INVALID_NAME(HttpStatus.BAD_REQUEST.value(), 400, "상품 이름 형식 오류 (2~50자 미준수, 특수문자/공백 포함)"),
  PRODUCT_INVALID_DESCRIPTION(HttpStatus.BAD_REQUEST.value(), 400,
      "상품 설명 길이 초과 (200자 초과) 또는 형식 오류"),
  PRODUCT_INVALID_PRICE(HttpStatus.BAD_REQUEST.value(), 400, "가격 범위 오류 (0원 미만 또는 50,000원 초과)"),
  PRODUCT_INVALID_CATEGORY(HttpStatus.BAD_REQUEST.value(), 400, "정의되지 않은 카테고리 값 입력"),
  PRODUCT_INVALID_STOCK(HttpStatus.BAD_REQUEST.value(), 400, "재고 수량 범위 오류 (0 미만 또는 10,000 초과)"),
  PRODUCT_INVALID_IMAGE(HttpStatus.BAD_REQUEST.value(), 400, "지원하지 않는 이미지 확장자이거나 잘못된 이미지 URL 형식"),
  PRODUCT_UNAUTHENTICATED(HttpStatus.UNAUTHORIZED.value(), 401, "상품을 등록하려면 관리자 계정으로 로그인이 필요합니다."),
  PRODUCT_FORBIDDEN(HttpStatus.FORBIDDEN.value(), 403, "상품 등록 권한이 없는 유저가 관리자 기능을 시도했습니다."),

  // 상품 조회
  PRODUCT_INVALID_PAGE(HttpStatus.BAD_REQUEST.value(), 400, "페이지 번호 형식 오류 (0 미만 또는 숫자가 아닌 값 입력)"),
  PRODUCT_INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST.value(), 400, "페이지 크기 형식 오류 (허용 범위를 벗어난 크기 요청)"),
  PRODUCT_INVALID_SORT(HttpStatus.BAD_REQUEST.value(), 400, "잘못된 정렬 파라미터 형식 또는 존재하지 않는 정렬 기준 요청"),
  PRODUCT_UNAUTHENTICATED_VIEW(HttpStatus.UNAUTHORIZED.value(), 401,
      "비공개 상품이거나 회원 전용 상품인 경우 로그인이 필요합니다."),
  PRODUCT_FORBIDDEN_VIEW(HttpStatus.FORBIDDEN.value(), 403, "해당 상품 정보를 조회할 권한이 없습니다."),
  PRODUCT_STATUS_CONFLICT(HttpStatus.CONFLICT.value(), 409,
      "상품 상태가 요청을 수행할 수 없는 상태입니다 (이미 삭제됨 또는 재고 부족)"),
  PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 404, "존재하지 않는 상품 아이디입니다."),

  // 상품 수정
  // (위의 INVALID 시리즈는 수정 시에도 공통 사용 가능)
  PRODUCT_UPDATE_UNAUTHENTICATED(HttpStatus.UNAUTHORIZED.value(), 401,
      "상품 수정을 하려면 관리자 계정으로 로그인이 필요합니다."),
  PRODUCT_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN.value(), 403, "해당 상품을 수정할 관리자 권한이 없습니다."),
  PRODUCT_UPDATE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 404,
      "수정하려는 상품 ID가 존재하지 않거나 이미 삭제된 상품입니다."),

  // 상품 삭제
  PRODUCT_DELETE_UNAUTHENTICATED(HttpStatus.UNAUTHORIZED.value(), 401,
      "상품을 삭제하려면 관리자 계정으로 로그인이 필요합니다."),
  PRODUCT_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN.value(), 403, "해당 상품을 삭제할 관리자 권한이 없습니다."),
  PRODUCT_DELETE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 404,
      "삭제하려는 상품 ID가 존재하지 않거나 이미 삭제 처리된 상품입니다."),
  PRODUCT_DELETE_FAILED(HttpStatus.CONFLICT.value(), 409, "해당 상품으로 결제된 주문 내역이 존재하여 삭제할 수 없습니다.");

  private final Integer httpStatus;
  private final Integer code;
  private final String message;

}
