package com.shoppingmall.ecommerceapi.domain.order.converter;

import com.shoppingmall.ecommerceapi.domain.order.dto.AdminOrderItemResponse;
import com.shoppingmall.ecommerceapi.domain.order.dto.AdminOrderResponse;
import com.shoppingmall.ecommerceapi.domain.order.dto.OrderItemResponse;
import com.shoppingmall.ecommerceapi.domain.order.dto.OrderResponse;
import com.shoppingmall.ecommerceapi.domain.order.entity.Order;
import com.shoppingmall.ecommerceapi.domain.order.entity.OrderItem;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderConverter {

  /**
   * Order 엔티티를 OrderResponse로 변환 (일반 사용자용)
   */
  public OrderResponse toResponse(Order order) {
    return OrderResponse.builder()
        .orderId(order.getId())
        .orderNumber(order.getOrderNumber())
        .userId(order.getUser().getId())
        .orderStatus(order.getOrderStatus())
        .zipCode(order.getZipCode())
        .address(order.getAddress())
        .detailAddress(order.getDetailAddress())
        .orderItems(toOrderItemResponseList(order.getOrderItems()))
        .totalPrice(order.getTotalPrice())
        .createdAt(order.getCreatedAt())
        .updatedAt(order.getUpdatedAt())
        .build();
  }

  /**
   * OrderItem 리스트를 OrderItemResponse 리스트로 변환
   */
  private List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> orderItems) {
    return orderItems.stream()
        .map(this::toOrderItemResponse)
        .collect(Collectors.toList());
  }

  /**
   * OrderItem 엔티티를 OrderItemResponse로 변환
   */
  private OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
    return OrderItemResponse.builder()
        .orderItemId(orderItem.getId())
        .productId(orderItem.getProduct().getId())
        .productName(orderItem.getProduct().getName())
        .quantity(orderItem.getQuantity())
        .orderPrice(orderItem.getOrderPrice())
        .totalPrice(orderItem.getTotalItemPrice())
        .build();
  }

  /**
   * Order 엔티티를 AdminOrderResponse로 변환 (관리자용)
   */
  public AdminOrderResponse toAdminResponse(Order order) {
    return AdminOrderResponse.builder()
        .orderId(order.getId())
        .orderNumber(order.getOrderNumber())
        .userId(order.getUser().getId())
        .username(order.getUser().getName())
        .userEmail(order.getUser().getEmail())
        .userPhone(order.getUser().getPhone())
        .orderStatus(order.getOrderStatus())
        .zipCode(order.getZipCode())
        .address(order.getAddress())
        .detailAddress(order.getDetailAddress())
        .orderItems(toAdminOrderItemResponseList(order.getOrderItems()))
        .totalPrice(order.getTotalPrice())
        .createdAt(order.getCreatedAt())
        .updatedAt(order.getUpdatedAt())
        .deletedAt(order.getDeletedAt())
        .build();
  }

  /**
   * OrderItem 리스트를 AdminOrderItemResponse 리스트로 변환
   */
  private List<AdminOrderItemResponse> toAdminOrderItemResponseList(List<OrderItem> orderItems) {
    return orderItems.stream()
        .map(this::toAdminOrderItemResponse)
        .collect(Collectors.toList());
  }

  /**
   * OrderItem 엔티티를 AdminOrderItemResponse로 변환
   */
  private AdminOrderItemResponse toAdminOrderItemResponse(OrderItem orderItem) {
    return AdminOrderItemResponse.builder()
        .orderItemId(orderItem.getId())
        .productId(orderItem.getProduct().getId())
        .productName(orderItem.getProduct().getName())
        .quantity(orderItem.getQuantity())
        .orderPrice(orderItem.getOrderPrice())
        .totalPrice(orderItem.getTotalItemPrice())
        .build();
  }
}
