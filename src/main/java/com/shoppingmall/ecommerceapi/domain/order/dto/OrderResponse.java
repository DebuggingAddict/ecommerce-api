package com.shoppingmall.ecommerceapi.domain.order.dto;

import com.shoppingmall.ecommerceapi.domain.order.entity.enums.OrderStatus;
import java.math.BigDecimal;
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
public class OrderResponse {

  private Long orderId;
  private String orderNumber;
  private Long userId;
  private OrderStatus orderStatus;
  private String zipCode;
  private String address;
  private String detailAddress;
  private List<OrderItemResponse> orderItems;
  private BigDecimal totalPrice;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
