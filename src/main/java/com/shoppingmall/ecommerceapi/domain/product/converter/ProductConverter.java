package com.shoppingmall.ecommerceapi.domain.product.converter;

import com.shoppingmall.ecommerceapi.domain.product.dto.ProductCreateRequest;
import com.shoppingmall.ecommerceapi.domain.product.dto.ProductResponse;
import com.shoppingmall.ecommerceapi.domain.product.entity.Product;
import com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductStatus;
import org.springframework.stereotype.Component;

@Component
public class ProductConverter {

  public Product toEntity(ProductCreateRequest request, String finalImgSrc, ProductStatus status) {
    return Product.builder()
        .name(request.getName())
        .price(request.getPrice())
        .description(request.getDescription())
        .category(request.getCategory())
        .stock(request.getStock())
        .status(status)
        .imgSrc(finalImgSrc)
        .build();
  }

  public ProductResponse toResponse(Product product) {
    if (product == null) {
      return null;
    }

    return ProductResponse.builder()
        .id(product.getId())
        .name(product.getName())
        .price(product.getPrice())
        .category(product.getCategory().name())
        .stock(product.getStock())
        .status(product.getStatus().name())
        .description(product.getDescription())
        .imgSrc(product.getImgSrc())
        .createdAt(product.getCreatedAt())
        .build();
  }
}