package com.shoppingmall.ecommerceapi.domain.order.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.shoppingmall.ecommerceapi.common.exception.BusinessException;
import com.shoppingmall.ecommerceapi.domain.order.dto.AdminOrderResponse;
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

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AdminOrderController.class)
class AdminOrderControllerTest {

  @Autowired
  MockMvc mockMvc;

  @MockitoBean
  OrderService orderService;

  private AdminOrderResponse adminOrderResponse() {
    return AdminOrderResponse.builder()
        .orderId(100L)
        .orderNumber("ORD_ADMIN_0001")
        .userId(1L)
        .zipCode("12345")
        .address("서울시 강남구")
        .detailAddress("101호")
        .totalPrice(BigDecimal.valueOf(20_000))
        .build();
  }

  @Test
  @DisplayName("관리자 주문 목록 조회 - 상태+유저 필터, 페이징 응답")
  void getAllOrders() throws Exception {
    AdminOrderResponse order = adminOrderResponse();
    Page<AdminOrderResponse> page = new PageImpl<>(
        List.of(order),
        PageRequest.of(0, 10),
        1
    );

    given(orderService.getAllOrders(eq("PENDING"), eq(1L), any()))
        .willReturn(page);

    mockMvc.perform(get("/api/admin/orders")
            .param("page", "0")
            .param("size", "10")
            .param("sortType", "PENDING")
            .param("sort", "createdAt,desc")
            .param("userId", "1"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.result.code").value(200))
        .andExpect(jsonPath("$.body.content[0].orderNumber").value("ORD_ADMIN_0001"))
        .andExpect(jsonPath("$.body.pageInfo.totalElements").value(1));

    verify(orderService).getAllOrders(eq("PENDING"), eq(1L), any());
  }

  @Test
  @DisplayName("관리자 주문 상세 조회 - 200, 응답 본문 검증")
  void getOrderForAdmin() throws Exception {
    Long orderId = 100L;
    AdminOrderResponse response = adminOrderResponse();

    given(orderService.getOrderForAdmin(orderId)).willReturn(response);

    mockMvc.perform(get("/api/admin/orders/{id}", orderId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.result.code").value(200))
        .andExpect(jsonPath("$.body.orderId").value(100L))
        .andExpect(jsonPath("$.body.orderNumber").value("ORD_ADMIN_0001"));

    verify(orderService).getOrderForAdmin(orderId);
  }

  @Test
  @DisplayName("관리자 주문 확정 - 200, 메시지 검증")
  void confirmOrder() throws Exception {
    Long orderId = 100L;

    mockMvc.perform(post("/api/admin/orders/{id}/confirm", orderId)
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.result.code").value(200))
        .andExpect(jsonPath("$.body").value("주문이 확정되었습니다"));

    verify(orderService).confirmOrder(orderId);
  }

  @Test
  @DisplayName("관리자 주문 상세 조회 - 주문이 없으면 ORDER_NOT_FOUND 에러코드 반환")
  void getOrderForAdmin_notFound() throws Exception {
    Long orderId = 999L;

    given(orderService.getOrderForAdmin(orderId))
        .willThrow(new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

    mockMvc.perform(get("/api/admin/orders/{id}", orderId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.result.code").value(OrderErrorCode.ORDER_NOT_FOUND.getHttpStatus()))
        .andExpect(jsonPath("$.result.message").value(OrderErrorCode.ORDER_NOT_FOUND.getMessage()))
        .andExpect(jsonPath("$.result.description").exists());
  }

  @Test
  @DisplayName("관리자 주문 확정 - 상태 충돌이면 ORDER_STATUS_CONFLICT 에러코드 반환")
  void confirmOrder_statusConflict() throws Exception {
    Long orderId = 100L;

    // void 메서드 예외 stubbing
    willThrow(new BusinessException(OrderErrorCode.ORDER_STATUS_CONFLICT))
        .given(orderService).confirmOrder(orderId);
    // doThrow(new BusinessException(OrderErrorCode.ORDER_STATUS_CONFLICT))
    //     .when(orderService).confirmOrder(orderId);

    mockMvc.perform(post("/api/admin/orders/{id}/confirm", orderId))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.result.code")
            .value(OrderErrorCode.ORDER_STATUS_CONFLICT.getHttpStatus()))
        .andExpect(jsonPath("$.result.message")
            .value(OrderErrorCode.ORDER_STATUS_CONFLICT.getMessage()))
        .andExpect(jsonPath("$.body").doesNotExist());
  }


}
