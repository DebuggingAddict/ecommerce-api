package com.shoppingmall.ecommerceapi.domain.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddCartItemRequest {

  @NotNull
  private Long productId;

  @Min(1)
  private int quantity;
}