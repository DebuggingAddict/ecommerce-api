package com.shoppingmall.ecommerceapi.domain.order.entity;

import com.shoppingmall.ecommerceapi.domain.product.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class OrderItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "order_item_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  @Setter
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "order_price", nullable = false, precision = 10, scale = 0)
  private BigDecimal orderPrice;

  // JPA Auditing 적용
  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  // 비즈니스 메서드
  public BigDecimal getTotalItemPrice() {
    return orderPrice.multiply(BigDecimal.valueOf(quantity));
  }

  public void updateQuantity(Integer newQuantity) {
    validateQuantityValue(newQuantity);
    this.quantity = newQuantity;
  }

  public void validateQuantity() {
    validateQuantityValue(this.quantity);
  }

  private void validateQuantityValue(Integer quantity) {
    if (quantity == null || quantity < 1 || quantity > 99) {
      throw new IllegalArgumentException("ORDER_ITEM_INVALID_QUANTITY");
    }
  }

  public void validatePrice() {
    if (orderPrice == null || orderPrice.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("ORDER_ITEM_INVALID_PRICE");
    }
  }
}
