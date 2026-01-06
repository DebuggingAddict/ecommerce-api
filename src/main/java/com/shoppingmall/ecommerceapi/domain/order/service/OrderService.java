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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final UserRepository userRepository;
  private final OrderConverter orderConverter;
  private final OrderNumberGenerator orderNumberGenerator;
  private final RedissonClient redissonClient;

  // lock 설정값
  private static final long LOCK_WAIT_TIME = 5L;     // 5초 대기
  private static final long LOCK_LEASE_TIME = 3L;    // 3초 유지
  private static final String PRODUCT_LOCK_PREFIX = "product:stock:";
  private static final String ORDER_NUMBER_LOCK = "order:number:generate";

  /**
   * 1. 주문 생성 POST /orders
   * 분산lock 적용: 상품별 재고 차감 (데드락 방지: ID 정렬)
   */
  @Transactional
  public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
    // 유저 검증
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_INVALID_USER));

    // 주문번호 생성 (분산lock 적용)
    String orderNumber = generateOrderNumberWithLock();

    // 주문 생성
    Order order = Order.builder()
            .user(user)
            .zipCode(request.getZipCode())
            .address(request.getAddress())
            .detailAddress(request.getDetailAddress())
            .totalPrice(request.getTotalPrice())
            .build();

    order.setOrderNumber(orderNumber);

    // 데드락 방지: 상품 ID 오름차순 정렬
    List<CreateOrderItemRequest> sortedItems = request.getOrderItems().stream()
            .sorted(Comparator.comparing(CreateOrderItemRequest::getProductId))
            .toList();

    log.info("주문 생성 시작 - 주문번호: {}, 상품 개수: {}, 정렬된 상품 ID: {}",
            orderNumber,
            sortedItems.size(),
            sortedItems.stream()
                    .map(CreateOrderItemRequest::getProductId)
                    .toList());

    BigDecimal calculatedTotal = BigDecimal.ZERO;

    // 각 상품별로 분산lock 적용하여 재고 차감
    for (CreateOrderItemRequest itemRequest : sortedItems)  {
      // 상품별 lock 키 생성
      String lockKey = PRODUCT_LOCK_PREFIX + itemRequest.getProductId();
      RLock lock = redissonClient.getLock(lockKey);

      try {
        // lock 획득 시도 (5초 대기, 3초 유지)
        boolean acquired = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);

        if (!acquired) {
          log.warn("상품 lock 획득 실패 - 상품 ID: {}", itemRequest.getProductId());
          throw new BusinessException(OrderErrorCode.ORDER_ITEM_OUT_OF_STOCK);
        }

        try {
          // 상품 조회
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

          // 재고 차감 (lock 안에서 실행)
          product.updateStock(-itemRequest.getQuantity());

          log.info("재고 차감 완료 - 상품 ID: {}, 남은 재고: {}",
                  product.getId(), product.getStock());

        } finally {
          // lock 해제
          if (lock.isHeldByCurrentThread()) {
            lock.unlock();
          }
        }

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.error("상품 lock 대기 중 인터럽트 발생 - 상품 ID: {}", itemRequest.getProductId(), e);
        throw new BusinessException(OrderErrorCode.ORDER_ITEM_OUT_OF_STOCK);
      }
    }

    // 총 금액 검증
    order.validateTotalPrice(calculatedTotal);

    // 주문 저장
    Order savedOrder = orderRepository.save(order);

    return orderConverter.toResponse(savedOrder);
  }

  /**
   * 주문번호 생성 (분산lock 적용)
   * 다중 서버 환경에서 중복 방지
   */
  private String generateOrderNumberWithLock() {
    RLock lock = redissonClient.getLock(ORDER_NUMBER_LOCK);

    try {
      // lock 획득 (3초 대기, 2초 유지)
      boolean acquired = lock.tryLock(3, 2, TimeUnit.SECONDS);

      if (!acquired) {
        log.warn("주문번호 생성 lock 획득 실패");
        throw new BusinessException(OrderErrorCode.ORDER_CREATION_FAILED);
      }

      try {
        Long todayOrderCount = orderRepository.countTodayOrders();
        Long nextSequence = todayOrderCount + 1;
        String orderNumber = orderNumberGenerator.generate(nextSequence);

        log.info("주문번호 생성 완료: {}", orderNumber);
        return orderNumber;

      } finally {
        if (lock.isHeldByCurrentThread()) {
          lock.unlock();
        }
      }

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("주문번호 생성 중 인터럽트 발생", e);
      throw new BusinessException(OrderErrorCode.ORDER_CREATION_FAILED);
    }
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
   * 분산lock 적용: 재고 복구 (데드락 방지: ID 정렬)
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

    // 데드락 방지: 상품 ID 오름차순 정렬
    List<OrderItem> sortedItems = order.getOrderItems().stream()
            .sorted(Comparator.comparing(item -> item.getProduct().getId()))
            .toList();

    log.info("주문 취소 시작 - 주문 ID: {}, 상품 개수: {}", orderId, sortedItems.size());

    // 각 상품별로 분산lock 적용하여 재고 복구
    List<RLock> acquiredLocks = new ArrayList<>();

    try {
      for (OrderItem orderItem : sortedItems) {
        Product product = orderItem.getProduct();
        String lockKey = PRODUCT_LOCK_PREFIX + product.getId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
          boolean acquired = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);

          if (!acquired) {
            log.warn("재고 복구 lock 획득 실패 - 상품 ID:: {}", product.getId());
            throw new BusinessException(OrderErrorCode.ORDER_CANCEL_FAILED);
          }

          acquiredLocks.add(lock);

          // 재고 복구
          product.updateStock(orderItem.getQuantity());
          log.info("재고 복구 완료 - 상품 ID: {}, 복구 수량: {}, 현재 재고: {}",
                  product.getId(), orderItem.getQuantity(), product.getStock());

        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          log.error("재고 복구 lock 대기 중 인터럽트 발생 - 상품 ID: {}", product.getId(), e);
          throw new BusinessException(OrderErrorCode.ORDER_CANCEL_FAILED);
        }
      }
      log.info("주문 취소 완료 - 주문 ID: {}", orderId);
    } finally {
      // 모든 획득한 lock 해제
      for (RLock lock : acquiredLocks) {
        if (lock.isHeldByCurrentThread()) {
          lock.unlock();
        }
      }
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