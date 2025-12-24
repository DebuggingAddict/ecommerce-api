package com.shoppingmall.ecommerceapi.domain.product.service;

import com.shoppingmall.ecommerceapi.domain.product.converter.ProductConverter;
import com.shoppingmall.ecommerceapi.domain.product.dto.ProductCreateRequest;
import com.shoppingmall.ecommerceapi.domain.product.dto.ProductResponse;
import com.shoppingmall.ecommerceapi.domain.product.entity.Product;
import com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductStatus;
import com.shoppingmall.ecommerceapi.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProductService {

  private final ProductRepository productRepository;
  private final ProductConverter productConverter;

  public ProductResponse register(ProductCreateRequest request) {
    String finalImgSrc = (request.getImgSrc() == null) ? "none.png" : request.getImgSrc();

    ProductStatus status =
        (request.getStock() == null || request.getStock() <= 0) ? ProductStatus.SOLD_OUT
            : ProductStatus.FOR_SALE;

    Product product = productConverter.toEntity(request, finalImgSrc, status);

    Product savedProduct = productRepository.save(product);
    return productConverter.toResponse(savedProduct);
  }

}
