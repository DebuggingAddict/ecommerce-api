package com.shoppingmall.ecommerceapi.domain.product.controller;

import static com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductCategory.FOOD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.ecommerceapi.domain.product.dto.ProductCreateRequest;
import com.shoppingmall.ecommerceapi.domain.product.dto.ProductResponse;
import com.shoppingmall.ecommerceapi.domain.product.dto.ProductUpdateRequest;
import com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductStatus;
import com.shoppingmall.ecommerceapi.domain.product.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(AdminProductController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminProductControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockitoBean
  ProductService productService;

  // 상품 등록 테스트
  @Test
  @DisplayName("POST /admin/v1/products - 상품 등록 성공")
  void createProduct_success() throws Exception {
    // given
    ProductCreateRequest request = ProductCreateRequest.builder()
        .name("딸기")
        .price(15000)
        .category(FOOD)
        .stock(100)
        .description("신선하고 맛있는 딸기!")
        .imgSrc(null)
        .build();

    ProductResponse response = ProductResponse.builder()
        .id(1L)
        .name("딸기")
        .price(15000)
        .category("FOOD")
        .stock(100)
        .status("FOR_SALE")
        .description("신선하고 맛있는 딸기!")
        .imgSrc("none.png")
        .build();

    given(productService.register(any(ProductCreateRequest.class))).willReturn(response);

    // when & then
    mockMvc.perform(post("/admin/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.body.name").value("딸기"))
        .andExpect(jsonPath("$.body.price").value("15000"))
        .andExpect(jsonPath("$.body.category").value("FOOD"))
        .andExpect(jsonPath("$.body.stock").value("100"))
        .andExpect(jsonPath("$.body.status").value("FOR_SALE"))
        .andExpect(jsonPath("$.body.description").value("신선하고 맛있는 딸기!"))
        .andExpect(jsonPath("$.body.imgSrc").value("none.png"));
  }

  // 상품 수정 테스트
  @Test
  @DisplayName("PUT /admin/v1/products/{id} - 상품 수정 성공")
  void updateProduct_success() throws Exception {
    // given
    Long productId = 1L;
    ProductUpdateRequest request = ProductUpdateRequest.builder()
        .name("수정된 딸기")
        .price(18000)
        .category(FOOD)
        .stock(50)
        .status(ProductStatus.FOR_SALE)
        .description("수정된 딸기 상품 설명")
        .imgSrc(null)
        .build();

    ProductResponse response = ProductResponse.builder()
        .id(productId)
        .name("수정된 딸기")
        .price(18000)
        .category("FOOD")
        .stock(50)
        .status("FOR_SALE")
        .description("수정된 딸기 상품 설명")
        .imgSrc("none.png")
        .build();

    given(productService.updateProduct(eq(productId), any(ProductUpdateRequest.class))).willReturn(
        response);

    // when & then
    mockMvc.perform(put("/admin/products/{id}", productId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.body.name").value("수정된 딸기"))
        .andExpect(jsonPath("$.body.price").value("18000"))
        .andExpect(jsonPath("$.body.category").value("FOOD"))
        .andExpect(jsonPath("$.body.stock").value("50"))
        .andExpect(jsonPath("$.body.status").value("FOR_SALE"))
        .andExpect(jsonPath("$.body.description").value("수정된 딸기 상품 설명"))
        .andExpect(jsonPath("$.body.imgSrc").value("none.png"));
  }

  // 상품 삭제 테스트
  @Test
  @DisplayName("PATCH /admin/products/{id} - 상품 삭제 성공")
  void deleteProduct_success() throws Exception {
    //given
    Long productId = 1L;
    willDoNothing().given(productService).deleteProduct(productId);

    // when & then
    mockMvc.perform(patch("/admin/products/{id}", productId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").exists());
  }

  // 필수값 누락 테스트
  @Test
  @DisplayName("POST /admin/api/products - 상품명 누락 시 400 반환")
  void createProduct_invalidName_400() throws Exception {
    // given
    ProductCreateRequest request = ProductCreateRequest.builder()
        .name("")
        .price(2000)
        .build();

    // when & then
    mockMvc.perform(post("/admin/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  // 등록 실패 - 재고 수량을 말도 안 되게 크게 등록할 때
  @Test
  @DisplayName("POST /admin/products - 재고 수량이 범위를 초과할 경우 400 반환")
  void createProduct_invalidStock_400() throws Exception {
    // given
    ProductCreateRequest request = ProductCreateRequest.builder()
        .name("딸기")
        .price(15000)
        .category(FOOD)
        .stock(999_999_999)
        .build();

    // when & then
    mockMvc.perform(post("/admin/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  // 수정 실패 - 수정 시 가격을 음수로 변경 시도
  @Test
  @DisplayName("PUT /admin/products/{id} - 가격을 음수로 수정 시 400 반환")
  void updateProduct_invalidPrice_400() throws Exception {
    // given
    Long productId = 1L;
    ProductUpdateRequest request = ProductUpdateRequest.builder()
        .name("딸기")
        .price(-1000)
        .category(FOOD)
        .status(ProductStatus.FOR_SALE)
        .build();

    // when & then
    mockMvc.perform(put("/admin/products/{id}", productId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  // 삭제 실패 - 이미 삭제되었거나 존재하지 않는 상품 삭제 시도
  @Test
  @DisplayName("PATCH /admin/products/{id} - 존재하지 않는 상품 삭제 시 404 반환")
  void deleteProduct_notFound_404() throws Exception {
    // given
    Long invalidId = 999L;
    willThrow(new com.shoppingmall.ecommerceapi.common.exception.BusinessException(
        com.shoppingmall.ecommerceapi.domain.product.exception.ProductErrorCode.PRODUCT_NOT_FOUND))
        .given(productService).deleteProduct(invalidId);

    // when & then
    mockMvc.perform(patch("/admin/products/{id}", invalidId))
        .andExpect(status().isNotFound());
  }
}
