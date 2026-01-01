package com.shoppingmall.ecommerceapi.domain.product.entity;

import com.shoppingmall.ecommerceapi.common.exception.BusinessException;
import com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductCategory;
import com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductStatus;
import com.shoppingmall.ecommerceapi.domain.product.exception.ProductErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 50)
  private String name;

  @Column(length = 200)
  private String description;

  @Column(nullable = false)
  private Integer price;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProductCategory category;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProductStatus status;

  @Column(nullable = false)
  private Integer stock = 0;

  @Column(nullable = false, length = 700)
  private String imgSrc = "none.png";

  @Builder.Default
  private Boolean isActive = true;

  @CreatedDate
  @Column(updatable = false, nullable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  private LocalDateTime deletedAt;

  // 상품 수정
  public void update(String name, String description, Integer price,
      ProductCategory category, ProductStatus status,
      Integer stock, String imgSrc) {
    // 재고 음수인가 검증
    validateStock(stock);
    // 재고 0인데 판매중으로 설정하려는가? 검증
    validateStatusWithStock(stock, status);

    this.name = name;
    this.description = description;
    this.price = price;
    this.category = category;
    this.status = status;
    this.stock = stock;
    this.imgSrc = imgSrc;
  }

  // 상품 삭제
  public void delete() {
    if (this.isActive == false) {
      throw new BusinessException(ProductErrorCode.PRODUCT_DELETE_NOT_FOUND);
    }

    this.isActive = false;
    this.deletedAt = LocalDateTime.now();
    this.status = ProductStatus.STOP_SALE;
  }

  // 재고 관리
  public void updateStock(Integer quantity) {
    if (quantity == null) {
      return;
    }

    int resultStock = this.stock + quantity;

    if (resultStock < 0) {
      throw new BusinessException(
          ProductErrorCode.PRODUCT_INVALID_STOCK,
          String.format("요청하신 수량만큼 차감할 수 없습니다.", this.stock, Math.abs(quantity))
      );
    }

    this.stock = resultStock;

    if (this.stock > 0) {
      this.status = ProductStatus.FOR_SALE;
    } else {
      this.status = ProductStatus.SOLD_OUT;
    }
  }

  // 내부 검증
  private void validateStock(int stock) {
    if (stock < 0) {
      throw new BusinessException(ProductErrorCode.PRODUCT_INVALID_STOCK);
    }
  }

  private void validateStatusWithStock(int stock, ProductStatus status) {
    if (stock == 0 && status == ProductStatus.FOR_SALE) {
      throw new BusinessException(ProductErrorCode.PRODUCT_STATUS_CONFLICT);
    }
  }
}
