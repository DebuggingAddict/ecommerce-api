package com.shoppingmall.ecommerceapi.domain.cart.controller;

import com.shoppingmall.ecommerceapi.common.api.Api;
import com.shoppingmall.ecommerceapi.domain.cart.converter.CartConverter;
import com.shoppingmall.ecommerceapi.domain.cart.dto.AddCartItemRequest;
import com.shoppingmall.ecommerceapi.domain.cart.dto.CartItemAddResponse;
import com.shoppingmall.ecommerceapi.domain.cart.dto.CartItemQuantityUpdateResponse;
import com.shoppingmall.ecommerceapi.domain.cart.dto.CartResponse;
import com.shoppingmall.ecommerceapi.domain.cart.dto.ChangeCartItemQuantityRequest;
import com.shoppingmall.ecommerceapi.domain.cart.entity.Cart;
import com.shoppingmall.ecommerceapi.domain.cart.entity.CartItem;
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
@RequestMapping("/api/carts")
public class CartController {

  private final CartService cartService;

  // 장바구니 조회 (GET /api/v1/cart)
  @GetMapping
  public Api<CartResponse> getCart(@RequestHeader("X-USER-ID") Long userId) {
    CartResponse response = cartService.getCartResponse(userId);
    return Api.OK(response);
  }

  // 장바구니 담기 (POST /api/v1/cart/items)
  @PostMapping("/items")
  public Api<CartItemAddResponse> addItem(
      @RequestHeader("X-USER-ID") Long userId,
      @Valid @RequestBody AddCartItemRequest request
  ) {
    // 서비스에서 Cart와 CartItem을 리턴하게 설계했다고 가정
    Cart cart = cartService.getCartByUserId(userId);
    CartItem item = cartService.addItem(userId, request); // addItem이 CartItem을 반환하도록 수정

    CartItemAddResponse response = CartConverter.toCartItemAddResponse(cart, item);
    return Api.OK(response);
  }

  // 수량 변경 (PATCH /api/v1/cart/items/{id})
  @PatchMapping("/items/{id}")
  public Api<CartItemQuantityUpdateResponse> changeQuantity(
      @RequestHeader("X-USER-ID") Long userId,
      @PathVariable Long id,   // cartItemId
      @Valid @RequestBody ChangeCartItemQuantityRequest request
  ) {
    CartItem item = cartService.changeQuantity(userId, id, request.getQuantity());
    CartItemQuantityUpdateResponse response =
        CartConverter.toCartItemQuantityUpdateResponse(item);
    return Api.OK(response);
  }


  // 특정 상품 삭제 (DELETE /api/v1/cart/items/{productId})
  @DeleteMapping("/items/{productId}")
  public Api<Void> removeItem(
      @RequestHeader("X-USER-ID") Long userId,
      @PathVariable Long productId
  ) {
    cartService.removeItem(userId, productId);
    return Api.OK(null);
  }

  // 장바구니 비우기 (DELETE /api/v1/cart/items)
  @DeleteMapping("/items")
  public Api<Void> clearCart(@RequestHeader("X-USER-ID") Long userId) {
    cartService.clearCart(userId);
    return Api.OK(null);
  }
}
