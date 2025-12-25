package com.shoppingmall.ecommerceapi.domain.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {

  private Long cartItemId;
  private Long productId;
  private int quantity;
}
