package com.shoppingmall.ecommerceapi.domain.order.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoppingmall.ecommerceapi.config.jpa.JpaAuditingConfig;
import com.shoppingmall.ecommerceapi.domain.order.entity.Order;
import com.shoppingmall.ecommerceapi.domain.order.entity.OrderItem;
import com.shoppingmall.ecommerceapi.domain.order.entity.enums.OrderStatus;
import com.shoppingmall.ecommerceapi.domain.product.entity.Product;
import com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductCategory;
import com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductStatus;
import com.shoppingmall.ecommerceapi.domain.product.repository.ProductRepository;
import com.shoppingmall.ecommerceapi.domain.user.entity.User;
import com.shoppingmall.ecommerceapi.domain.user.entity.enums.UserRole;
import com.shoppingmall.ecommerceapi.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
class OrderRepositoryTest {

  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final UserRepository userRepository;
  private final ProductRepository productRepository;

  @Autowired
  OrderRepositoryTest(
      OrderRepository orderRepository,
      OrderItemRepository orderItemRepository,
      UserRepository userRepository,
      ProductRepository productRepository
  ) {
    this.orderRepository = orderRepository;
    this.orderItemRepository = orderItemRepository;
    this.userRepository = userRepository;
    this.productRepository = productRepository;
  }

  private User savedUser;
  private Product savedProduct;
  private Order savedOrder;

  @BeforeEach
  void setUp() {
    orderItemRepository.deleteAll();
    orderRepository.deleteAll();

    savedUser = userRepository.save(User.builder()
        .name("테스트유저")
        .email("test@example.com")
        .phone("010-1234-5678")
        .role(UserRole.USER)
        .build());

    savedProduct = productRepository.save(Product.builder()
        .name("테스트상품")
        .description("테스트 상품 설명")
        .price(10_000)
        .category(ProductCategory.FOOD)
        .status(ProductStatus.FOR_SALE)
        .stock(100)
        .imgSrc("none.png")
        .build());

    // 1) Order 먼저 생성
    savedOrder = Order.builder()
        .orderNumber("ORD_TEST_0001")
        .user(savedUser)
        .orderStatus(OrderStatus.PENDING)
        .zipCode("12345")
        .address("서울시 강남구")
        .detailAddress("101호")
        .totalPrice(BigDecimal.valueOf(20_000))
        .build();

    // 2) OrderItem 생성 후 연관관계 메서드로 묶기
    OrderItem orderItem = OrderItem.builder()
        .product(savedProduct)
        .quantity(2)
        .orderPrice(10_000)
        .build();

    savedOrder.addOrderItem(orderItem);  // 양방향 연관관계 세팅

    // 3) Cascade.ALL 이므로 Order만 저장하면 OrderItem도 함께 저장됨
    savedOrder = orderRepository.save(savedOrder);
  }


  @Test
  @DisplayName("ID로 주문과 주문 아이템을 함께 조회한다")
  void findByIdWithItems() {
    // when
    var result = orderRepository.findByIdWithItems(savedOrder.getId());

    // then
    assertThat(result).isPresent();
    Order order = result.get();
    assertThat(order.getId()).isEqualTo(savedOrder.getId());
    assertThat(order.getOrderItems()).hasSize(1);
    assertThat(order.getDeletedAt()).isNull();
  }

  @Test
  @DisplayName("삭제된 주문은 ID로 조회되지 않는다")
  void findByIdWithItems_WhenDeleted() {
    // given
    savedOrder.delete();
    orderRepository.save(savedOrder);

    // when
    var result = orderRepository.findByIdWithItems(savedOrder.getId());

    // then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("사용자 ID로 주문 목록을 페이징 조회한다")
  void findByUserId() {
    Pageable pageable = PageRequest.of(0, 10);

    Page<Order> page = orderRepository.findByUserId(savedUser.getId(), pageable);

    assertThat(page.getContent()).hasSize(1);
    Order first = page.getContent().get(0);
    assertThat(first.getUser().getId()).isEqualTo(savedUser.getId());
    assertThat(first.getDeletedAt()).isNull();
  }

  @Test
  @DisplayName("사용자 ID와 주문 상태로 주문 목록을 페이징 조회한다")
  void findByUserIdAndOrderStatus() {
    Pageable pageable = PageRequest.of(0, 10);

    Page<Order> page =
        orderRepository.findByUserIdAndOrderStatus(savedUser.getId(), OrderStatus.PENDING,
            pageable);

    assertThat(page.getContent()).hasSize(1);
    Order first = page.getContent().get(0);
    assertThat(first.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
    assertThat(first.getDeletedAt()).isNull();
  }

  @Test
  @DisplayName("다른 주문 상태로 조회하면 결과가 없다")
  void findByUserIdAndOrderStatus_DifferentStatus() {
    Pageable pageable = PageRequest.of(0, 10);

    Page<Order> page =
        orderRepository.findByUserIdAndOrderStatus(savedUser.getId(), OrderStatus.PAID, pageable);

    assertThat(page).isEmpty();
  }

  @Test
  @DisplayName("삭제되지 않은 모든 주문을 조회한다")
  void findAllNotDeleted() {
    // given: 삭제된 주문 하나 추가
    Order deletedOrder = orderRepository.save(Order.builder()
        .orderNumber("ORD_TEST_0002")
        .user(savedUser)
        .orderStatus(OrderStatus.CANCELLED)
        .zipCode("12345")
        .address("서울시 강남구")
        .detailAddress("202호")
        .totalPrice(BigDecimal.valueOf(30_000))
        .build());
    deletedOrder.delete();
    orderRepository.save(deletedOrder);

    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<Order> page = orderRepository.findAllNotDeleted(pageable);

    // then
    assertThat(page.getContent()).hasSize(1);
    Order first = page.getContent().get(0);
    assertThat(first.getId()).isEqualTo(savedOrder.getId());
    assertThat(first.getDeletedAt()).isNull();
  }

  @Test
  @DisplayName("특정 주문 상태의 주문들을 조회한다")
  void findAllByOrderStatus() {
    // given: PAID 주문 하나 추가
    orderRepository.save(Order.builder()
        .orderNumber("ORD_TEST_0003")
        .user(savedUser)
        .orderStatus(OrderStatus.PAID)
        .zipCode("12345")
        .address("서울시 강남구")
        .detailAddress("303호")
        .totalPrice(BigDecimal.valueOf(40_000))
        .build());

    Pageable pageable = PageRequest.of(0, 10);

    Page<Order> pending = orderRepository.findAllByOrderStatus(OrderStatus.PENDING, pageable);
    Page<Order> paid = orderRepository.findAllByOrderStatus(OrderStatus.PAID, pageable);

    assertThat(pending.getContent()).hasSize(1);
    assertThat(pending.getContent().get(0).getOrderStatus()).isEqualTo(OrderStatus.PENDING);

    assertThat(paid.getContent()).hasSize(1);
    assertThat(paid.getContent().get(0).getOrderStatus()).isEqualTo(OrderStatus.PAID);
  }

  @Test
  @DisplayName("주문 번호로 주문을 조회한다")
  void findByOrderNumber() {
    var result = orderRepository.findByOrderNumber("ORD_TEST_0001");

    assertThat(result).isPresent();
    Order order = result.get();
    assertThat(order.getOrderNumber()).isEqualTo("ORD_TEST_0001");
    assertThat(order.getDeletedAt()).isNull();
  }

  @Test
  @DisplayName("존재하지 않는 주문 번호로 조회하면 빈 결과를 반환한다")
  void findByOrderNumber_NotExists() {
    var result = orderRepository.findByOrderNumber("ORD_NOT_EXISTS");

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("오늘 생성된 주문 수를 카운트한다")
  void countTodayOrders() {
    orderRepository.save(Order.builder()
        .orderNumber("ORD_TEST_0004")
        .user(savedUser)
        .orderStatus(OrderStatus.PENDING)
        .zipCode("12345")
        .address("서울시 강남구")
        .detailAddress("404호")
        .totalPrice(BigDecimal.valueOf(50_000))
        .build());

    Long count = orderRepository.countTodayOrders();

    assertThat(count).isGreaterThanOrEqualTo(2L);
  }
}
