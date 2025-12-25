package com.shoppingmall.ecommerceapi.domain.cart.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "carts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Cart {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false, unique = true)
  private Long userId;

  // Cart는 유저 생성 시 함께 만들어지고 “삭제 없이 계속 사용”한다는 전제라 createdAt 생략
  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
  private final List<CartItem> items = new ArrayList<>();

  @Builder
  private Cart(Long userId) {
    this.userId = userId;
  }

  // 양방향 동기화: add로만 추가하게 유도
  public void addItem(CartItem item) {
    items.add(item);
    item.attachCart(this);
  }

  public void removeItem(CartItem item) {
    items.remove(item);
    item.detachCart();
  }

  // “카트는 유지, 아이템만 초기화”
  // orphanRemoval=true라 컬렉션에서 제거되면 자식 엔티티 삭제 대상으로 잡힘
  public void clearItems() {
    for (CartItem item : items) {
      item.detachCart();
    }
    items.clear();
  }
}
