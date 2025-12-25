package com.shoppingmall.ecommerceapi.domain.product.controller;

import com.shoppingmall.ecommerceapi.common.api.Api;
import com.shoppingmall.ecommerceapi.common.response.PageRequestDTO;
import com.shoppingmall.ecommerceapi.common.response.PageResponse;
import com.shoppingmall.ecommerceapi.domain.product.dto.ProductResponse;
import com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductCategory;
import com.shoppingmall.ecommerceapi.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/open-api/products")
@RequiredArgsConstructor
public class OpenApiProductController {

  private final ProductService productService;

  // 상품 전체 조회
  @GetMapping("/open-api/products")
  public Api<PageResponse<ProductResponse>> getProducts(
      @RequestParam(required = false) ProductCategory category,
      PageRequestDTO pageRequestDTO
  ) {
    PageResponse<ProductResponse> response = productService.getProducts(category, pageRequestDTO);

    return Api.OK(response);
  }

  // 상품 단건 조회
  @GetMapping("/open-api/products/{id}")
  public Api<ProductResponse> getProduct(
      @PathVariable Long id
  ) {
    ProductResponse response = productService.getProduct(id);

    return Api.OK(response);
  }

}
