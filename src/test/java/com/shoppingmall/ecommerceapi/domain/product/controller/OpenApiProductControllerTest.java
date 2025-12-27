package com.shoppingmall.ecommerceapi.domain.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.ecommerceapi.domain.cart.service.CartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(OpenApiProductController.class)
@AutoConfigureMockMvc(addFilters = false)
public class OpenApiProductControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockitoBean
  CartService productService;

  // 전체 조회 테스트
  @Test
  @DisplayName("Get /open-api/products - 전체 상품 조회 성공")
  void getProducts_success() throws Exception {
    // given

  }

}
