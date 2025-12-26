package com.shoppingmall.ecommerceapi.domain.order.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminOrderItemResponse {

  private Long orderItemId;
  private Long productId;
  private String productName;
  private Integer quantity;
  private Integer orderPrice;
  private BigDecimal totalPrice;
}
