package com.shoppingmall.ecommerceapi.domain.cart.converter;

import com.shoppingmall.ecommerceapi.domain.cart.dto.AddCartItemRequest;
import com.shoppingmall.ecommerceapi.domain.cart.dto.CartItemResponse;
import com.shoppingmall.ecommerceapi.domain.cart.dto.CartResponse;
import com.shoppingmall.ecommerceapi.domain.cart.entity.Cart;
import com.shoppingmall.ecommerceapi.domain.cart.entity.CartItem;
import java.util.List;

public class CartConverter {

  private CartConverter() {
  }

  public static CartItem toEntity(AddCartItemRequest request) {
    return CartItem.builder()
        .productId(request.getProductId())
        .quantity(request.getQuantity())
        .build();
  }

  public static CartItemResponse toResponse(CartItem item) {
    return CartItemResponse.builder()
        .cartItemId(item.getId())
        .productId(item.getProductId())
        .quantity(item.getQuantity())
        .build();
  }

  public static CartResponse toResponse(Cart cart) {
    List<CartItemResponse> items = cart.getItems().stream()
        .map(CartConverter::toResponse)
        .toList();

    return CartResponse.builder()
        .cartId(cart.getId())
        .userId(cart.getUserId())
        .items(items)
        .build();
  }
}
