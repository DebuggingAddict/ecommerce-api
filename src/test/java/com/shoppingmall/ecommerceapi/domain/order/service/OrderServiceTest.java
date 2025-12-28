package com.shoppingmall.ecommerceapi.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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
import com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductCategory;
import com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductStatus;
import com.shoppingmall.ecommerceapi.domain.product.repository.ProductRepository;
import com.shoppingmall.ecommerceapi.domain.user.entity.User;
import com.shoppingmall.ecommerceapi.domain.user.entity.enums.UserRole;
import com.shoppingmall.ecommerceapi.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @Mock
  private OrderRepository orderRepository;
  @Mock
  private ProductRepository productRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private OrderConverter orderConverter;
  @Mock
  private OrderNumberGenerator orderNumberGenerator;

  @InjectMocks
  private OrderService orderService;

  @BeforeEach
  void setUp() {
    orderRepository.deleteAll();
  }

  private User createUser() {
    return User.builder()
        .id(1L)
        .username("testuser")
        .password("password123")
        .name("테스트유저")
        .email("test@example.com")
        .phone("010-1234-5678")
        .role(UserRole.USER)
        .build();
  }

  private Product createProduct() {
    return Product.builder()
        .id(10L)
        .name("상품A")
        .description("테스트 상품")
        .price(10_000)
        .category(ProductCategory.FOOD)
        .status(ProductStatus.FOR_SALE)
        .stock(100)
        .imgSrc("img.png")
        .build();
  }

  @Test
  @DisplayName("주문 생성 - 정상 흐름")
  void createOrder_success() {
    // given
    Long userId = 1L;
    User user = createUser();
    Product product = createProduct();

    CreateOrderItemRequest itemReq = CreateOrderItemRequest.builder()
        .productId(product.getId())
        .quantity(2)
        .build();

    CreateOrderRequest req = CreateOrderRequest.builder()
        .zipCode("12345")
        .address("서울시 강남구")
        .detailAddress("101호")
        .totalPrice(BigDecimal.valueOf(20_000))
        .orderItems(List.of(itemReq))
        .build();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(productRepository.findById(product.getId())).willReturn(Optional.of(product));
    given(orderRepository.countTodayOrders()).willReturn(0L);
    given(orderNumberGenerator.generate(1L)).willReturn("ORD20241228001");

    ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);

    Order savedOrder = Order.builder()
        .id(100L)
        .user(user)
        .orderNumber("ORD20241228001")
        .zipCode(req.getZipCode())
        .address(req.getAddress())
        .detailAddress(req.getDetailAddress())
        .orderStatus(OrderStatus.PENDING)
        .totalPrice(req.getTotalPrice())
        .build();
    given(orderRepository.save(any(Order.class))).willReturn(savedOrder);

    OrderResponse response = OrderResponse.builder().build();
    given(orderConverter.toResponse(savedOrder)).willReturn(response);

    // when
    OrderResponse result = orderService.createOrder(userId, req);

    // then
    assertThat(result).isSameAs(response);
    verify(orderRepository).save(orderCaptor.capture());
    Order captured = orderCaptor.getValue();

    assertThat(captured.getUser().getId()).isEqualTo(userId);
    assertThat(captured.getOrderNumber()).isEqualTo("ORD20241228001");
    assertThat(captured.getOrderItems()).hasSize(1);
    assertThat(captured.getTotalPrice()).isEqualTo(BigDecimal.valueOf(20_000));
    assertThat(product.getStock()).isEqualTo(98);  // 재고 2 감소
  }

  @Test
  @DisplayName("주문 생성 - 유저가 없으면 예외")
  void createOrder_invalidUser() {
    Long userId = 1L;
    CreateOrderRequest req = CreateOrderRequest.builder().build();

    given(userRepository.findById(userId)).willReturn(Optional.empty());

    BusinessException ex = assertThrows(
        BusinessException.class,
        () -> orderService.createOrder(userId, req)
    );

    assertThat(ex.getCode()).isEqualTo(OrderErrorCode.ORDER_INVALID_USER);
  }

  @Test
  @DisplayName("주문 생성 - 상품이 없으면 예외")
  void createOrder_invalidProduct() {
    Long userId = 1L;
    User user = createUser();

    CreateOrderItemRequest itemReq = CreateOrderItemRequest.builder()
        .productId(999L)
        .quantity(1)
        .build();

    CreateOrderRequest req = CreateOrderRequest.builder()
        .zipCode("12345")
        .address("서울")
        .detailAddress("101호")
        .totalPrice(BigDecimal.valueOf(10_000))
        .orderItems(List.of(itemReq))
        .build();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(productRepository.findById(999L)).willReturn(Optional.empty());

    BusinessException ex = assertThrows(
        BusinessException.class,
        () -> orderService.createOrder(userId, req)
    );

    assertThat(ex.getCode()).isEqualTo(OrderErrorCode.ORDER_ITEM_INVALID_PRODUCT);
  }


  @Test
  @DisplayName("내 주문 상세 조회 - 성공")
  void getOrder_success() {
    Long orderId = 100L;
    Long userId = 1L;
    User user = createUser();

    Order order = Order.builder()
        .id(orderId)
        .user(user)
        .orderNumber("ORD_TEST")
        .zipCode("12345")
        .address("서울")
        .detailAddress("101호")
        .totalPrice(BigDecimal.valueOf(10_000))
        .build();

    OrderResponse response = OrderResponse.builder().build();

    given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(order));
    given(orderConverter.toResponse(order)).willReturn(response);

    OrderResponse result = orderService.getOrder(orderId, userId);

    assertThat(result).isSameAs(response);
  }

  @Test
  @DisplayName("내 주문 상세 조회 - 다른 사람 주문이면 예외")
  void getOrder_forbidden() {
    Long orderId = 100L;
    Long userId = 1L;
    User otherUser = createUser().builder().id(2L).build();

    Order order = Order.builder()
        .id(orderId)
        .user(otherUser)
        .orderNumber("ORD_TEST")
        .zipCode("12345")
        .address("서울")
        .detailAddress("101호")
        .totalPrice(BigDecimal.valueOf(10_000))
        .build();

    given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(order));

    BusinessException ex = assertThrows(
        BusinessException.class,
        () -> orderService.getOrder(orderId, userId)
    );

    assertThat(ex.getCode()).isEqualTo(OrderErrorCode.ORDER_FORBIDDEN);
  }

  @Test
  @DisplayName("주문 취소 - 재고 복구")
  void cancelOrder_restoreStock() {
    Long orderId = 100L;
    Long userId = 1L;
    User user = createUser();
    Product product = createProduct().builder().stock(10).build();

    OrderItem orderItem = OrderItem.builder()
        .product(product)
        .quantity(3)
        .orderPrice(product.getPrice())
        .build();

    Order order = Order.builder()
        .id(orderId)
        .user(user)
        .orderNumber("ORD_TEST")
        .zipCode("12345")
        .address("서울")
        .detailAddress("101호")
        .orderStatus(OrderStatus.PENDING)
        .totalPrice(BigDecimal.valueOf(30_000))
        .build();
    order.addOrderItem(orderItem);

    given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(order));

    orderService.cancelOrder(orderId, userId);

    assertThat(product.getStock()).isEqualTo(13); // 10 + 3
    assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
  }

  @Test
  @DisplayName("관리자 주문 상세 조회 - 성공")
  void getOrderForAdmin_success() {
    Long orderId = 100L;

    Order order = Order.builder()
        .id(orderId)
        .orderNumber("ORD_TEST")
        .totalPrice(BigDecimal.valueOf(10_000))
        .build();

    AdminOrderResponse response = AdminOrderResponse.builder().build();

    given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(order));
    given(orderConverter.toAdminResponse(order)).willReturn(response);

    AdminOrderResponse result = orderService.getOrderForAdmin(orderId);

    assertThat(result).isSameAs(response);
  }

  @Test
  @DisplayName("관리자 주문 확정 - 상태 변경")
  void confirmOrder_success() {
    Long orderId = 100L;

    Order order = Order.builder()
        .id(orderId)
        .orderNumber("ORD_TEST")
        .orderStatus(OrderStatus.PENDING)
        .totalPrice(BigDecimal.valueOf(10_000))
        .build();

    given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

    orderService.confirmOrder(orderId);

    assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);
  }
}
