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

  private Long id;                    // cart_items.id
  private Long productId;             // product_id
  private String productName;         // product_name
  private Integer productPrice;       // product_price
  private Integer productQuantity;    // product_quantity
}
