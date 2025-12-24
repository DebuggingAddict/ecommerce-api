package com.shoppingmall.ecommerceapi.domain.product.entity;

import com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductCategory;
import com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
  private ProductStatus status = ProductStatus.SOLD_OUT;

  @Column(nullable = false)
  private Integer stock = 0;

  @Builder.Default
  @Column(nullable = false)
  private String imgSrc = "none.png";

  private Boolean isActive = true;

  private LocalDateTime createdAt = LocalDateTime.now();

  private LocalDateTime updatedAt;

  private LocalDateTime deletedAt;
}
