package com.shoppingmall.ecommerceapi.domain.cart.converter;

import com.shoppingmall.ecommerceapi.domain.cart.dto.AddCartItemRequest;
import com.shoppingmall.ecommerceapi.domain.cart.dto.CartItemAddResponse;
import com.shoppingmall.ecommerceapi.domain.cart.dto.CartItemQuantityUpdateResponse;
import com.shoppingmall.ecommerceapi.domain.cart.dto.CartItemResponse;
import com.shoppingmall.ecommerceapi.domain.cart.dto.CartResponse;
import com.shoppingmall.ecommerceapi.domain.cart.entity.Cart;
import com.shoppingmall.ecommerceapi.domain.cart.entity.CartItem;
import java.util.List;

public class CartConverter {

  private CartConverter() {
  }

  // ---------- toEntity ----------

  public static CartItem toEntity(AddCartItemRequest request) {
    return CartItem.builder()
        .productId(request.getProductId())
        .quantity(request.getQuantity())
        .build();
  }

  // ---------- toResponse (조회용) ----------

  // GET /carts 의 items 요소
  public static CartItemResponse toCartItemResponse(CartItem item) {
    return CartItemResponse.builder()
        .id(item.getId())                         // cart_items.id
        .productId(item.getProductId())          // product_id
        .productName(null)                       // TODO: Product 연동 후 세팅
        .productPrice(null)                      // TODO: Product 연동 후 세팅
        .productQuantity(item.getQuantity())     // quantity
        .build();
  }

  // GET /carts 응답 본문
  public static CartResponse toCartResponse(Cart cart) {
    List<CartItemResponse> items = cart.getItems().stream()
        .map(CartConverter::toCartItemResponse)
        .toList();

    return CartResponse.builder()
        .cartId(cart.getId())
        .userId(cart.getUserId())
        .updatedAt(cart.getUpdatedAt())
        .items(items)
        .build();
  }

  // ---------- toResponse (아이템 추가 응답) ----------

  // POST /carts/items 응답
  public static CartItemAddResponse toCartItemAddResponse(Cart cart, CartItem item) {
    return CartItemAddResponse.builder()
        .id(item.getId())
        .cartId(cart.getId())
        .productId(item.getProductId())
        .productQuantity(item.getQuantity())
        .createdAt(item.getCreatedAt())
        .updatedAt(item.getUpdatedAt())
        .build();
  }

  // ---------- toResponse (수량 변경 응답) ----------

  // PATCH /carts/items/{id} 응답
  public static CartItemQuantityUpdateResponse toCartItemQuantityUpdateResponse(CartItem item) {
    return CartItemQuantityUpdateResponse.builder()
        .id(item.getId())
        .productId(item.getProductId())
        .productQuantity(item.getQuantity())
        .updatedAt(item.getUpdatedAt())
        .build();
  }
}
