package com.shoppingmall.ecommerceapi.domain.cart.entity;

import com.shoppingmall.ecommerceapi.common.exception.BusinessException;
import com.shoppingmall.ecommerceapi.domain.cart.exception.CartErrorCode;
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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@Table(
    name = "cart_items",
    uniqueConstraints = @UniqueConstraint(columnNames = {"cart_id", "product_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CartItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cart_id", nullable = false)
  private Cart cart;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "quantity", nullable = false)
  private int quantity;

  @CreatedDate
  @Column(name = "created_at", updatable = false, nullable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  // 생성은 Builder로 (단, 연관관계는 Cart.addItem을 통해 들어오게 하는 걸 추천)
  @Builder
  private CartItem(Long productId, int quantity) {
    this.productId = productId;
    this.quantity = quantity;
  }

  // Cart 쪽에서만 호출해서 양방향 일관성 유지
  void attachCart(Cart cart) {
    this.cart = cart;
  }

  void detachCart() {
    this.cart = null;
  }

  public void changeQuantity(int quantity) {
    if (quantity <= 0) {
      throw new BusinessException(
          CartErrorCode.CART_ITEM_INVALID_QUANTITY,
          "상품 수량은 항상 양수여야합니다."
      );
    }
    this.quantity = quantity;
  }

}

