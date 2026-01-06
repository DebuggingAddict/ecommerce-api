package com.shoppingmall.ecommerceapi.domain.order.exception;

import com.shoppingmall.ecommerceapi.common.code.ApiCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum OrderErrorCode implements ApiCode {

  // 주문 관련
  ORDER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 404, "주문을 찾을 수 없습니다"),
  ORDER_FORBIDDEN(HttpStatus.FORBIDDEN.value(), 403, "해당 주문에 접근할 권한이 없습니다"),
  ORDER_INVALID_USER(HttpStatus.BAD_REQUEST.value(), 400, "유효하지 않은 사용자입니다"),
  ORDER_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST.value(), 400, "주문 금액이 일치하지 않습니다"),
  ORDER_STATUS_CONFLICT(HttpStatus.BAD_REQUEST.value(), 400, "주문 상태가 유효하지 않습니다"),
  ORDER_ALREADY_PAID_OR_CANCELLED(HttpStatus.BAD_REQUEST.value(), 400, "이미 결제되었거나 취소된 주문입니다"),

  // 주문 아이템 관련
  ORDER_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 404, "주문 아이템을 찾을 수 없습니다"),
  ORDER_ITEM_INVALID_PRODUCT(HttpStatus.BAD_REQUEST.value(), 400, "유효하지 않은 상품입니다"),
  ORDER_ITEM_INVALID_QUANTITY(HttpStatus.BAD_REQUEST.value(), 400, "수량은 1~99 사이여야 합니다"),
  ORDER_ITEM_INVALID_PRICE(HttpStatus.BAD_REQUEST.value(), 400, "가격은 0보다 커야 합니다"),
  ORDER_ITEM_OUT_OF_STOCK(HttpStatus.BAD_REQUEST.value(), 400, "재고가 부족합니다"),
  ORDER_MUST_HAVE_AT_LEAST_ONE_ITEM(HttpStatus.BAD_REQUEST.value(), 400, "주문은 최소 1개 이상의 상품이 필요합니다"),

  // 분산 락
  ORDER_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(),500, "주문 생성에 실패했습니다. 잠시 후 다시 시도해주세요."),
  ORDER_CANCEL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(),500,"주문 취소에 실패했습니다. 잠시 후 다시 시도해주세요.");

  private final Integer httpStatus;
  private final Integer code;
  private final String message;
}