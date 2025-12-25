package com.shoppingmall.ecommerceapi.domain.product.controller;

import com.shoppingmall.ecommerceapi.domain.product.dto.ProductCreateRequest;
import com.shoppingmall.ecommerceapi.domain.product.dto.ProductResponse;
import com.shoppingmall.ecommerceapi.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/admin/products")
@RestController
public class AdminProductController {

  private final ProductService productService;

  @PostMapping("/admin/products")
  public ResponseEntity<ProductResponse> create(@RequestBody ProductCreateRequest request) {
    return ResponseEntity.ok(productService.register(request));
  }

}
