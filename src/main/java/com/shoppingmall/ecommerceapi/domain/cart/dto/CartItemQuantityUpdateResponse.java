package com.shoppingmall.ecommerceapi.domain.cart.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemQuantityUpdateResponse {

  private Long id;
  private Long productId;
  private Integer productQuantity;
  private LocalDateTime updatedAt;
}

