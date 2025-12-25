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
public class CartItemAddResponse {

  private Long id;
  private Long cartId;
  private Long productId;
  private Integer productQuantity;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}

