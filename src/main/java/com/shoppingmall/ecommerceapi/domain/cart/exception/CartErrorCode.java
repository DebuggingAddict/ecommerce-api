package com.shoppingmall.ecommerceapi.domain.cart.exception;

import com.shoppingmall.ecommerceapi.common.code.ApiCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

// CartErrorCode.java
@Getter
@AllArgsConstructor
public enum CartErrorCode implements ApiCode {

  // 장바구니 본체 관련
  CART_UNAUTHENTICATED(401, 401, "로그인이 필요합니다"),
  CART_FORBIDDEN(403, 403, "장바구니 접근 권한이 없습니다"),
  CART_NOT_FOUND(404, 403, "장바구니를 찾을 수 없습니다"),
  CART_ALREADY_EMPTY(400, 400, "이미 비어 있는 장바구니입니다"),
  CART_CLEANUP_FAILED(500, 500, "장바구니 비우기 중 오류가 발생했습니다"),
  CART_ALREADY_EXISTS(409, 409, "이미 존재하는 장바구니입니다"),

  // 장바구니 아이템 공통/권한
  CART_ITEM_UNAUTHENTICATED(401, 401, "로그인이 필요합니다"),
  CART_ITEM_FORBIDDEN(403, 403, "장바구니 아이템 접근 권한이 없습니다"),
  CART_ITEM_NOT_FOUND(404, 404, "장바구니 아이템을 찾을 수 없습니다"),

  // 상품/중복
  CART_ITEM_INVALID_PRODUCT(400, 400, "유효하지 않은 상품입니다"),
  CART_ITEM_PRODUCT_NOT_FOR_SALE(400, 400, "판매 불가 상태의 상품입니다"),
  CART_ITEM_ALREADY_EXISTS(409, 409, "이미 장바구니에 담긴 상품입니다"),

  // 수량/재고
  CART_ITEM_INVALID_QUANTITY(400, 400, "허용되지 않는 수량입니다"),
  CART_ITEM_OUT_OF_STOCK(400, 400, "재고가 부족합니다");

  private final Integer httpStatus;
  private final Integer code;
  private final String message;
}
