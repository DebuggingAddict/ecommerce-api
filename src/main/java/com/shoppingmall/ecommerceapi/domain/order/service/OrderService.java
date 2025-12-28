package com.shoppingmall.ecommerceapi.domain.order.service;

import com.shoppingmall.ecommerceapi.common.exception.BusinessException;
import com.shoppingmall.ecommerceapi.domain.order.converter.OrderConverter;
import com.shoppingmall.ecommerceapi.domain.order.dto.AdminOrderResponse;
import com.shoppingmall.ecommerceapi.domain.order.dto.CreateOrderItemRequest;
import com.shoppingmall.ecommerceapi.domain.order.dto.CreateOrderRequest;
import com.shoppingmall.ecommerceapi.domain.order.dto.OrderResponse;
import com.shoppingmall.ecommerceapi.domain.order.entity.Order;
import com.shoppingmall.ecommerceapi.domain.order.entity.OrderItem;
import com.shoppingmall.ecommerceapi.domain.order.entity.enums.OrderStatus;
import com.shoppingmall.ecommerceapi.domain.order.exception.OrderErrorCode;
import com.shoppingmall.ecommerceapi.domain.order.repository.OrderRepository;
import com.shoppingmall.ecommerceapi.domain.order.util.OrderNumberGenerator;
import com.shoppingmall.ecommerceapi.domain.product.entity.Product;
import com.shoppingmall.ecommerceapi.domain.product.repository.ProductRepository;
import com.shoppingmall.ecommerceapi.domain.user.entity.User;
import com.shoppingmall.ecommerceapi.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final UserRepository userRepository;
  private final OrderConverter orderConverter;
  private final OrderNumberGenerator orderNumberGenerator;

  /**
   * 1. 주문 생성 POST /orders
   */
  @Transactional
  public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
    // 유저 검증
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_INVALID_USER));

    // 주문번호 생성
    String orderNumber = generateOrderNumber();

    // 주문 생성
    Order order = Order.builder()
        .user(user)
        .zipCode(request.getZipCode())
        .address(request.getAddress())
        .detailAddress(request.getDetailAddress())
        .totalPrice(request.getTotalPrice())
        .build();

    order.setOrderNumber(orderNumber);

    BigDecimal calculatedTotal = BigDecimal.ZERO;

    // 주문 아이템 생성 및 검증
    for (CreateOrderItemRequest itemRequest : request.getOrderItems()) {
      Product product = productRepository.findById(itemRequest.getProductId())
          .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_ITEM_INVALID_PRODUCT));

      // 재고 검증
      if (product.getStock() < itemRequest.getQuantity()) {
        throw new BusinessException(OrderErrorCode.ORDER_ITEM_OUT_OF_STOCK);
      }

      // OrderItem 생성
      OrderItem orderItem = OrderItem.builder()
          .product(product)
          .quantity(itemRequest.getQuantity())
          .orderPrice(product.getPrice())
          .build();

      orderItem.validateQuantity();
      orderItem.validatePrice();

      order.addOrderItem(orderItem);
      calculatedTotal = calculatedTotal.add(orderItem.getTotalItemPrice());

      // 재고 차감 (delta = -quantity)
      product.updateStock(-itemRequest.getQuantity());

    }

    // 총 금액 검증
    order.validateTotalPrice(calculatedTotal);

    // 주문 저장
    Order savedOrder = orderRepository.save(order);

    return orderConverter.toResponse(savedOrder);
  }

  /**
   * 주문번호 생성 (날짜 + 6자리 일련번호)
   */
  private synchronized String generateOrderNumber() {
    Long todayOrderCount = orderRepository.countTodayOrders();
    Long nextSequence = todayOrderCount + 1;
    return orderNumberGenerator.generate(nextSequence);
  }

  /**
   * 2. 내 주문 조회 (페이징 + 상태 필터링) GET /orders
   */
  public Page<OrderResponse> getMyOrders(Long userId, String statusFilter, Pageable pageable) {
    Page<Order> orders;

    // 상태 필터링
    if (statusFilter != null && !statusFilter.isEmpty()) {
      try {
        OrderStatus orderStatus = OrderStatus.valueOf(statusFilter.toUpperCase());
        orders = orderRepository.findByUserIdAndOrderStatus(userId, orderStatus, pageable);
      } catch (IllegalArgumentException e) {
        orders = orderRepository.findByUserId(userId, pageable);
      }
    } else {
      orders = orderRepository.findByUserId(userId, pageable);
    }

    return orders.map(orderConverter::toResponse);
  }

  /**
   * 3. 내 주문 상세 조회 GET /orders/{id}
   */
  public OrderResponse getOrder(Long orderId, Long userId) {
    Order order = orderRepository.findByIdWithItems(orderId)
        .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

    // 권한 검증
    if (!order.getUser().getId().equals(userId)) {
      throw new BusinessException(OrderErrorCode.ORDER_FORBIDDEN);
    }

    return orderConverter.toResponse(order);
  }

  /**
   * 4. 주문 취소 (결제 완료 전에만 가능) POST /orders/{id}/cancel
   */
  @Transactional
  public void cancelOrder(Long orderId, Long userId) {
    Order order = orderRepository.findByIdWithItems(orderId)
        .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

    // 권한 검증
    if (!order.getUser().getId().equals(userId)) {
      throw new BusinessException(OrderErrorCode.ORDER_FORBIDDEN);
    }

    // 주문 취소
    order.cancel();

    // 재고 복구
    for (OrderItem orderItem : order.getOrderItems()) {
      Product product = orderItem.getProduct();
      product.updateStock(orderItem.getQuantity());
    }
  }

  /**
   * 5. 내 주문내역 삭제 (소프트 삭제) PATCH /orders/{id}
   */
  @Transactional
  public void deleteOrder(Long orderId, Long userId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

    // 권한 검증
    if (!order.getUser().getId().equals(userId)) {
      throw new BusinessException(OrderErrorCode.ORDER_FORBIDDEN);
    }

    order.delete();
  }

  /**
   * 6. 관리자 주문 전체 조회 (페이징 + 상태/유저 필터링) GET /admin/orders
   */
  public Page<AdminOrderResponse> getAllOrders(String statusFilter, Long userIdFilter,
      Pageable pageable) {
    Page<Order> orders;

    // 상태 + 유저 필터링
    if (statusFilter != null && !statusFilter.isEmpty() && userIdFilter != null) {
      OrderStatus orderStatus = OrderStatus.valueOf(statusFilter.toUpperCase());
      orders = orderRepository.findByUserIdAndOrderStatus(userIdFilter, orderStatus, pageable);
    }
    // 상태만 필터링
    else if (statusFilter != null && !statusFilter.isEmpty()) {
      OrderStatus orderStatus = OrderStatus.valueOf(statusFilter.toUpperCase());
      orders = orderRepository.findAllByOrderStatus(orderStatus, pageable);
    }
    // 유저만 필터링
    else if (userIdFilter != null) {
      orders = orderRepository.findByUserId(userIdFilter, pageable);
    }
    // 전체 조회
    else {
      orders = orderRepository.findAllNotDeleted(pageable);
    }

    return orders.map(orderConverter::toAdminResponse);
  }

  /**
   * 7. 관리자 주문상세조회 GET /admin/orders/{id}
   */
  public AdminOrderResponse getOrderForAdmin(Long orderId) {
    Order order = orderRepository.findByIdWithItems(orderId)
        .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

    return orderConverter.toAdminResponse(order);
  }

  /**
   * 8. 관리자 주문 확정 (결제 완료 처리) POST /admin/orders/{id}/confirm
   */
  @Transactional
  public void confirmOrder(Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

    order.confirmPayment();
  }
}
