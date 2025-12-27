package com.shoppingmall.ecommerceapi.domain.order.entity;

import com.shoppingmall.ecommerceapi.common.exception.BusinessException;
import com.shoppingmall.ecommerceapi.domain.order.entity.enums.OrderStatus;
import com.shoppingmall.ecommerceapi.domain.order.exception.OrderErrorCode;
import com.shoppingmall.ecommerceapi.domain.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "order_id")
  private Long id;

  @Column(name = "order_number", nullable = false, unique = true, length = 20)
  private String orderNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "order_status", nullable = false)
  @Builder.Default
  private OrderStatus orderStatus = OrderStatus.PENDING;

  @Column(name = "zip_code", nullable = false, length = 5)
  private String zipCode;

  @Column(name = "address", nullable = false, length = 200)
  private String address;

  @Column(name = "detail_address", nullable = false, length = 100)
  private String detailAddress;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<OrderItem> orderItems = new ArrayList<>();

  @Column(name = "total_price", nullable = false, precision = 10, scale = 0)
  private BigDecimal totalPrice;

  // JPA Auditing 적용
  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  private LocalDateTime deletedAt;

  // 비즈니스 메서드
  public void setOrderNumber(String orderNumber) {
    this.orderNumber = orderNumber;
  }

  public void addOrderItem(OrderItem orderItem) {
    orderItems.add(orderItem);
    orderItem.setOrder(this);
  }

  public void updateTotalPrice(BigDecimal newTotalPrice) {
    this.totalPrice = newTotalPrice;
  }

  public void cancel() {
    if (this.orderStatus != OrderStatus.PENDING) {
      throw new BusinessException(OrderErrorCode.ORDER_STATUS_CONFLICT);
    }
    this.orderStatus = OrderStatus.CANCELLED;
  }

  public void confirmPayment() {
    if (this.orderStatus != OrderStatus.PENDING) {
      throw new BusinessException(OrderErrorCode.ORDER_STATUS_CONFLICT);
    }
    this.orderStatus = OrderStatus.PAID;
  }

  public void validateTotalPrice(BigDecimal calculatedTotal) {
    if (!this.totalPrice.equals(calculatedTotal)) {
      throw new BusinessException(OrderErrorCode.ORDER_AMOUNT_MISMATCH);
    }
  }

  public void delete() {
    this.deletedAt = LocalDateTime.now();
  }
}
