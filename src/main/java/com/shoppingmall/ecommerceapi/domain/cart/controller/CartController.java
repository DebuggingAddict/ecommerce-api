package com.shoppingmall.ecommerceapi.domain.cart.controller;

import com.shoppingmall.ecommerceapi.common.api.Api;
import com.shoppingmall.ecommerceapi.domain.cart.converter.CartConverter;
import com.shoppingmall.ecommerceapi.domain.cart.dto.AddCartItemRequest;
import com.shoppingmall.ecommerceapi.domain.cart.dto.CartResponse;
import com.shoppingmall.ecommerceapi.domain.cart.dto.ChangeCartItemQuantityRequest;
import com.shoppingmall.ecommerceapi.domain.cart.entity.Cart;
import com.shoppingmall.ecommerceapi.domain.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cart")
public class CartController {

  private final CartService cartService;

  // 더미 유저: 헤더로 userId 받기 (ex: X-USER-ID: 1)
  // 장바구니 조회
  @GetMapping
  public Api<CartResponse> getCart(@RequestHeader("X-USER-ID") Long userId) {
    Cart cart = cartService.getCartByUserId(userId);
    // TODO: paging 처리
    return Api.OK(CartConverter.toResponse(cart));
  }

  // 장바구니 담기
  @PostMapping("/items")
  public Api<Void> addItem(
      @RequestHeader("X-USER-ID") Long userId,
      @Valid @RequestBody AddCartItemRequest request
  ) {
    cartService.addItem(userId, request);
    return Api.OK(null);
  }

  // 수량 변경
  @PatchMapping("/items/quantity")
  public Api<Void> changeQuantity(
      @RequestHeader("X-USER-ID") Long userId,
      @Valid @RequestBody ChangeCartItemQuantityRequest request
  ) {
    cartService.changeQuantity(userId, request.getProductId(), request.getQuantity());
    return Api.OK(null);
  }

  // 특정 상품 삭제
  @DeleteMapping("/items/{productId}")
  public Api<Void> removeItem(
      @RequestHeader("X-USER-ID") Long userId,
      @PathVariable Long productId
  ) {
    cartService.removeItem(userId, productId);
    return Api.OK(null);
  }

  // 장바구니 비우기
  @DeleteMapping("/items")
  public Api<Void> clearCart(@RequestHeader("X-USER-ID") Long userId) {
    cartService.clearCart(userId);
    return Api.OK(null);
  }
}
