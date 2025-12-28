package com.shoppingmall.ecommerceapi.domain.order.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.ecommerceapi.common.exception.BusinessException;
import com.shoppingmall.ecommerceapi.domain.order.dto.CreateOrderItemRequest;
import com.shoppingmall.ecommerceapi.domain.order.dto.CreateOrderRequest;
import com.shoppingmall.ecommerceapi.domain.order.dto.OrderResponse;
import com.shoppingmall.ecommerceapi.domain.order.exception.OrderErrorCode;
import com.shoppingmall.ecommerceapi.domain.order.service.OrderService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockitoBean
  OrderService orderService;

  private CreateOrderRequest createOrderRequest() {
    CreateOrderItemRequest item = CreateOrderItemRequest.builder()
        .productId(1L)
        .quantity(2)
        .build();

    return CreateOrderRequest.builder()
        .zipCode("12345")
        .address("서울시 강남구")
        .detailAddress("101호")
        .totalPrice(BigDecimal.valueOf(20_000))
        .orderItems(List.of(item))
        .build();
  }

  private OrderResponse orderResponse() {
    return OrderResponse.builder()
        .orderId(100L)
        .orderNumber("ORD_TEST_0001")
        .zipCode("12345")
        .address("서울시 강남구")
        .detailAddress("101호")
        .totalPrice(BigDecimal.valueOf(20_000))
        .build();
  }

  @Test
  @DisplayName("주문 생성 API - 201, 응답 본문 검증")
  void createOrder() throws Exception {
    Long userId = 1L;
    CreateOrderRequest request = createOrderRequest();
    OrderResponse response = orderResponse();

    given(orderService.createOrder(eq(userId), any(CreateOrderRequest.class)))
        .willReturn(response);

    mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-User-Id", userId)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result.code").value(201))
        .andExpect(jsonPath("$.body.orderNumber").value("ORD_TEST_0001"));

    verify(orderService).createOrder(eq(userId), any(CreateOrderRequest.class));
  }

  @Test
  @DisplayName("내 주문 목록 조회 API - 200, 페이징 응답")
  void getMyOrders() throws Exception {
    Long userId = 1L;
    OrderResponse order = orderResponse();
    Page<OrderResponse> page = new PageImpl<>(
        List.of(order),
        PageRequest.of(0, 10),
        1
    );

    given(orderService.getMyOrders(eq(userId), eq("PENDING"), any()))
        .willReturn(page);

    mockMvc.perform(get("/api/orders")
            .header("X-User-Id", userId)
            .param("page", "0")
            .param("size", "10")
            .param("sortType", "PENDING")
            .param("sort", "createdAt,desc"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result.code").value(200))
        .andExpect(jsonPath("$.body.content[0].orderNumber").value("ORD_TEST_0001"))
        .andExpect(jsonPath("$.body.pageInfo.totalElements").value(1));

    verify(orderService).getMyOrders(eq(userId), eq("PENDING"), any());
  }

  @Test
  @DisplayName("내 주문 상세 조회 API - 200")
  void getOrder() throws Exception {
    Long userId = 1L;
    Long orderId = 100L;
    OrderResponse response = orderResponse();

    given(orderService.getOrder(orderId, userId)).willReturn(response);

    mockMvc.perform(get("/api/orders/{id}", orderId)
            .header("X-User-Id", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result.code").value(200))
        .andExpect(jsonPath("$.body.orderId").value(100L));

    verify(orderService).getOrder(orderId, userId);
  }

  @Test
  @DisplayName("주문 취소 API - 200, 메시지 검증")
  void cancelOrder() throws Exception {
    Long userId = 1L;
    Long orderId = 100L;

    mockMvc.perform(post("/api/orders/{id}/cancel", orderId)
            .header("X-User-Id", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result.code").value(200))
        .andExpect(jsonPath("$.body").value("주문이 취소되었습니다"));

    verify(orderService).cancelOrder(orderId, userId);
  }

  @Test
  @DisplayName("주문 삭제 API - 200, 메시지 검증")
  void deleteOrder() throws Exception {
    Long userId = 1L;
    Long orderId = 100L;

    mockMvc.perform(delete("/api/orders/{id}", orderId)
            .header("X-User-Id", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result.code").value(200))
        .andExpect(jsonPath("$.body").value("주문이 삭제되었습니다"));

    verify(orderService).deleteOrder(orderId, userId);
  }

  @Test
  @DisplayName("내 주문 상세 조회 - 다른 사용자가 조회하면 ORDER_FORBIDDEN 에러코드 반환")
  void getOrder_forbidden() throws Exception {
    Long userId = 1L;
    Long orderId = 100L;

    given(orderService.getOrder(orderId, userId))
        .willThrow(new BusinessException(OrderErrorCode.ORDER_FORBIDDEN));

    mockMvc.perform(get("/api/orders/{id}", orderId)
            .header("X-User-Id", userId))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.result.code").value(OrderErrorCode.ORDER_FORBIDDEN.getHttpStatus()))
        .andExpect(jsonPath("$.result.message").value(OrderErrorCode.ORDER_FORBIDDEN.getMessage()))
        .andExpect(jsonPath("$.body").doesNotExist());
  }

  @Test
  @DisplayName("내 주문 상세 조회 - 주문이 없으면 ORDER_NOT_FOUND 에러코드 반환")
  void getOrder_notFound() throws Exception {
    Long userId = 1L;
    Long orderId = 999L;

    given(orderService.getOrder(orderId, userId))
        .willThrow(new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

    mockMvc.perform(get("/api/orders/{id}", orderId)
            .header("X-User-Id", userId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.result.code")
            .value(OrderErrorCode.ORDER_NOT_FOUND.getHttpStatus()))
        .andExpect(jsonPath("$.result.message")
            .value(OrderErrorCode.ORDER_NOT_FOUND.getMessage()))
        .andExpect(jsonPath("$.body").doesNotExist());

    verify(orderService).getOrder(orderId, userId);
  }
  

  @Test
  @DisplayName("주문 취소 - 상태 충돌이면 ORDER_STATUS_CONFLICT 에러코드 반환")
  void cancelOrder_statusConflict() throws Exception {
    Long userId = 1L;
    Long orderId = 100L;

    willThrow(new BusinessException(OrderErrorCode.ORDER_STATUS_CONFLICT))
        .given(orderService).cancelOrder(orderId, userId);

    mockMvc.perform(post("/api/orders/{id}/cancel", orderId)
            .header("X-User-Id", userId))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.result.code")
            .value(OrderErrorCode.ORDER_STATUS_CONFLICT.getHttpStatus()))
        .andExpect(jsonPath("$.result.message")
            .value(OrderErrorCode.ORDER_STATUS_CONFLICT.getMessage()))
        .andExpect(jsonPath("$.body").doesNotExist());

    verify(orderService).cancelOrder(orderId, userId);
  }
}
