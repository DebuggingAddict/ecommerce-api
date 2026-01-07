package com.shoppingmall.ecommerceapi.domain.product.controller;

import com.shoppingmall.ecommerceapi.common.api.Api;
import com.shoppingmall.ecommerceapi.domain.product.dto.ProductCreateRequest;
import com.shoppingmall.ecommerceapi.domain.product.dto.ProductResponse;
import com.shoppingmall.ecommerceapi.domain.product.dto.ProductUpdateRequest;
import com.shoppingmall.ecommerceapi.domain.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RequestMapping("/api/admin/products")
@RestController
public class AdminProductController {

  private final ProductService productService;

  // 상품 등록
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Api<ProductResponse> create(
      @Valid @RequestPart(value = "request") ProductCreateRequest request,
      @RequestPart(value = "image", required = false) MultipartFile image
  ) {
    ProductResponse response = productService.register(request, image);

    return Api.CREATED(response);
  }

  // 상품 수정
  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Api<ProductResponse> update(
      @PathVariable Long id,
      @Valid @RequestPart(value = "request") ProductUpdateRequest request,
      @RequestPart(value = "image", required = false) MultipartFile image
  ) {
    ProductResponse response = productService.updateProduct(id, request, image);

    return Api.OK(response);
  }

  // 상품 삭제
  @PatchMapping("/{id}")
  public Api<Void> delete(
      @PathVariable Long id) {
    productService.deleteProduct(id);

    return Api.OK(null);
  }
}
