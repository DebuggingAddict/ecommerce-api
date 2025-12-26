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

  // CartItem + Product 정보를 조합해 단건 응답으로 변환
  public static CartItemResponse toCartItemResponse(
      CartItem item,
      String productName,
      Integer productPrice
  ) {
    return CartItemResponse.builder()
        .id(item.getId())                     // cart_items.id
        .productId(item.getProductId())       // product_id
        .productName(productName)             // product.name
        .productPrice(productPrice)           // product.price
        .productQuantity(item.getQuantity())  // quantity
        .build();
  }

  // GET /cart 응답 본문
  public static CartResponse toCartResponse(Cart cart, List<CartItemResponse> items) {
    return CartResponse.builder()
        .cartId(cart.getId())
        .userId(cart.getUserId())
        .updatedAt(cart.getUpdatedAt())
        .items(items)
        .build();
  }

  // ---------- toResponse (아이템 추가 응답) ----------

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

  public static CartItemQuantityUpdateResponse toCartItemQuantityUpdateResponse(CartItem item) {
    return CartItemQuantityUpdateResponse.builder()
        .id(item.getId())
        .productId(item.getProductId())
        .productQuantity(item.getQuantity())
        .updatedAt(item.getUpdatedAt())
        .build();
  }
}

