package com.shoppingmall.ecommerceapi.domain.order.controller;

import com.shoppingmall.ecommerceapi.common.api.Api;
import com.shoppingmall.ecommerceapi.common.response.PageRequestDTO;
import com.shoppingmall.ecommerceapi.common.response.PageResponse;
import com.shoppingmall.ecommerceapi.domain.order.dto.CreateOrderRequest;
import com.shoppingmall.ecommerceapi.domain.order.dto.OrderResponse;
import com.shoppingmall.ecommerceapi.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  /**
   * 주문 생성 POST /orders
   */
  @PostMapping
  public Api<OrderResponse> createOrder(
      @RequestHeader("X-User-Id") Long userId,
      @Valid @RequestBody CreateOrderRequest request) {
    OrderResponse response = orderService.createOrder(userId, request);
    return Api.CREATED(response);
  }

  /**
   * 내 주문 조회 (페이징 + 상태 필터링) GET /orders?page=0&size=10&sort=createdAt,desc&sortType=PENDING
   */
  @GetMapping
  public Api<PageResponse<OrderResponse>> getMyOrders(
      @RequestHeader("X-User-Id") Long userId,
      PageRequestDTO pageRequestDTO) {

    Page<OrderResponse> orderPage = orderService.getMyOrders(
        userId,
        pageRequestDTO.getSortType(),  // 상태 필터 (PENDING, PAID, CANCELLED)
        pageRequestDTO.toPageable()
    );

    PageResponse<OrderResponse> pageResponse = PageResponse.of(
        orderPage,
        orderPage.getContent(),
        pageRequestDTO.getSortType()
    );

    return Api.OK(pageResponse);
  }

  /**
   * 내 주문 상세 조회 GET /orders/{id}
   */
  @GetMapping("/{id}")
  public Api<OrderResponse> getOrder(
      @RequestHeader("X-User-Id") Long userId,
      @PathVariable Long id) {
    OrderResponse order = orderService.getOrder(id, userId);
    return Api.OK(order);
  }

  /**
   * 주문 취소 POST /orders/{id}/cancel
   */
  @PostMapping("/{id}/cancel")
  public Api<String> cancelOrder(
      @RequestHeader("X-User-Id") Long userId,
      @PathVariable Long id) {
    orderService.cancelOrder(id, userId);
    return Api.OK("주문이 취소되었습니다");
  }

  /**
   * 내 주문내역 삭제 (소프트 삭제) PATCH /orders/{id}
   */
  @DeleteMapping("/{id}")
  public Api<String> deleteOrder(
      @RequestHeader("X-User-Id") Long userId,
      @PathVariable Long id) {
    orderService.deleteOrder(id, userId);
    return Api.OK("주문이 삭제되었습니다");
  }
}
