package com.shoppingmall.ecommerceapi.domain.cart.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeCartItemQuantityRequest {

  @Min(1)
  private int quantity;
}

