package com.shoppingmall.ecommerceapi.domain.cart.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {

  private Long cartId;
  private Long userId;
  private LocalDateTime updatedAt;
  private List<CartItemResponse> items;
}
