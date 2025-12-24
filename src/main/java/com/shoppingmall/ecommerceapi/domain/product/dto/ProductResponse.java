package com.shoppingmall.ecommerceapi.domain.product.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

  private Long id;
  private String name;
  private Integer price;
  private String category;
  private Integer stock;
  private String status;
  private String description;
  private String imgSrc;
  private LocalDateTime createdAt;
}
