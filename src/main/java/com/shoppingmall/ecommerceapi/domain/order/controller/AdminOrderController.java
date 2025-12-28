package com.shoppingmall.ecommerceapi.domain.order.controller;

import com.shoppingmall.ecommerceapi.common.api.Api;
import com.shoppingmall.ecommerceapi.common.response.PageRequestDTO;
import com.shoppingmall.ecommerceapi.common.response.PageResponse;
import com.shoppingmall.ecommerceapi.domain.order.dto.AdminOrderResponse;
import com.shoppingmall.ecommerceapi.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

  private final OrderService orderService;

  /**
   * 관리자 주문 전체 조회 (페이징 + 필터링) GET
   * /admin/orders?page=0&size=10&sort=createdAt,desc&sortType=PENDING&userId=1
   * <p>
   * 쿼리 파라미터: - page: 페이지 번호 (기본값: 0) - size: 페이지 크기 (기본값: 10) - sort: 정렬 기준 (예: "createdAt,desc") -
   * sortType: 주문 상태 필터 (PENDING, PAID, CANCELLED) - Optional - userId: 특정 사용자 필터 - Optional
   */
  @GetMapping
  public Api<PageResponse<AdminOrderResponse>> getAllOrders(
      PageRequestDTO pageRequestDTO,
      @RequestParam(required = false) Long userId) {

    Page<AdminOrderResponse> orderPage = orderService.getAllOrders(
        pageRequestDTO.getSortType(),  // 상태 필터
        userId,                         // 유저 필터
        pageRequestDTO.toPageable()
    );

    PageResponse<AdminOrderResponse> pageResponse = PageResponse.of(
        orderPage,
        orderPage.getContent(),
        pageRequestDTO.getSort()
    );

    return Api.OK(pageResponse);
  }

  /**
   * 관리자 주문상세조회 GET /admin/orders/{id}
   */
  @GetMapping("/{id}")
  public Api<AdminOrderResponse> getOrder(@PathVariable Long id) {
    AdminOrderResponse order = orderService.getOrderForAdmin(id);
    return Api.OK(order);
  }

  /**
   * 관리자 주문 확정 (결제 완료 처리) POST /admin/orders/{id}/confirm
   */
  @PostMapping("/{id}/confirm")
  public Api<String> confirmOrder(@PathVariable Long id) {
    orderService.confirmOrder(id);
    return Api.OK("주문이 확정되었습니다");
  }
}
