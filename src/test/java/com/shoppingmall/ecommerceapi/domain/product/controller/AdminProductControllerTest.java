package com.shoppingmall.ecommerceapi.domain.product.controller;

import static com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductCategory.FOOD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.ecommerceapi.common.exception.BusinessException;
import com.shoppingmall.ecommerceapi.domain.product.dto.ProductCreateRequest;
import com.shoppingmall.ecommerceapi.domain.product.dto.ProductResponse;
import com.shoppingmall.ecommerceapi.domain.product.dto.ProductUpdateRequest;
import com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductStatus;
import com.shoppingmall.ecommerceapi.domain.product.exception.ProductErrorCode;
import com.shoppingmall.ecommerceapi.domain.product.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
  @DisplayName("POST /api/admin/products - 상품 등록 성공")
  void createProduct_success() throws Exception {
    // given
    ProductCreateRequest request = ProductCreateRequest.builder()
        .name("딸기")
        .price(15000)
        .category(FOOD)
        .stock(100)
        .description("신선하고 맛있는 딸기!")
        .build();

    MockMultipartFile requestPart = new MockMultipartFile(
        "request",
        "request",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    MockMultipartFile imagePart = new MockMultipartFile(
        "image",
        "strawberry.png",
        MediaType.IMAGE_PNG_VALUE,
        "test image content".getBytes()
    );

    ProductResponse response = ProductResponse.builder()
        .id(1L)
        .name("딸기")
        .price(15000)
        .category("FOOD")
        .stock(100)
        .status("FOR_SALE")
        .description("신선하고 맛있는 딸기!")
        .imgSrc("https://s3.com/strawberry.png")
        .build();

    given(productService.register(any(), any())).willReturn(response);

    // when & then
    mockMvc.perform(multipart("/api/admin/products")
            .file(requestPart)
            .file(imagePart)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.body.name").value("딸기"))
        .andExpect(jsonPath("$.body.price").value("15000"))
        .andExpect(jsonPath("$.body.category").value("FOOD"))
        .andExpect(jsonPath("$.body.stock").value("100"))
        .andExpect(jsonPath("$.body.status").value("FOR_SALE"))
        .andExpect(jsonPath("$.body.description").value("신선하고 맛있는 딸기!"))
        .andExpect(jsonPath("$.body.imgSrc").value("https://s3.com/strawberry.png"));
  }

  // 상품 수정 테스트
  @Test
  @DisplayName("PUT /api/admin/products/{id} - 상품 수정 성공")
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
        .build();

    MockMultipartFile requestPart = new MockMultipartFile(
        "request",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    MockMultipartFile imagePart = new MockMultipartFile(
        "image",
        "updated_strawberry.png",
        MediaType.IMAGE_PNG_VALUE,
        "new image content".getBytes()
    );

    ProductResponse response = ProductResponse.builder()
        .id(productId)
        .name("수정된 딸기")
        .price(18000)
        .category("FOOD")
        .stock(50)
        .status("FOR_SALE")
        .description("수정된 딸기 상품 설명")
        .imgSrc("https://s3.com/updated_strawberry.png")
        .build();

    given(productService.updateProduct(eq(productId), any(ProductUpdateRequest.class),
        any())).willReturn(
        response);

    // when & then
    mockMvc.perform(multipart(HttpMethod.PUT, "/api/admin/products/{id}", productId)
            .file(requestPart)
            .file(imagePart)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.body.name").value("수정된 딸기"))
        .andExpect(jsonPath("$.body.price").value("18000"))
        .andExpect(jsonPath("$.body.category").value("FOOD"))
        .andExpect(jsonPath("$.body.stock").value("50"))
        .andExpect(jsonPath("$.body.status").value("FOR_SALE"))
        .andExpect(jsonPath("$.body.description").value("수정된 딸기 상품 설명"))
        .andExpect(jsonPath("$.body.imgSrc").value("https://s3.com/updated_strawberry.png"));
  }

  // 상품 삭제 테스트
  @Test
  @DisplayName("PATCH /api/admin/products/{id} - 상품 삭제 성공")
  void deleteProduct_success() throws Exception {
    //given
    Long productId = 1L;
    willDoNothing().given(productService).deleteProduct(productId);

    // when & then
    mockMvc.perform(patch("/api/admin/products/{id}", productId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").exists());
  }

  // 필수값 누락 테스트
  @Test
  @DisplayName("POST /api/admin/products - 상품명 누락 시 400 반환")
  void createProduct_invalidName_400() throws Exception {
    // given
    ProductCreateRequest request = ProductCreateRequest.builder()
        .name("")
        .price(2000)
        .build();

    MockMultipartFile requestPart = new MockMultipartFile(
        "request",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    // when & then
    mockMvc.perform(multipart("/api/admin/products")
            .file(requestPart))
        .andExpect(status().isBadRequest());
  }

  // 등록 실패 - 재고 수량을 말도 안 되게 크게 등록할 때
  @Test
  @DisplayName("POST /api/admin/products - 재고 수량이 범위를 초과할 경우 400 반환")
  void createProduct_invalidStock_400() throws Exception {
    // given
    ProductCreateRequest request = ProductCreateRequest.builder()
        .name("딸기")
        .price(15000)
        .category(FOOD)
        .stock(999_999_999)
        .build();

    MockMultipartFile requestPart = new MockMultipartFile(
        "request",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    // when & then
    mockMvc.perform(multipart("/api/admin/products")
            .file(requestPart))
        .andExpect(status().isBadRequest());
  }

  // 등록 실패 - 지원하지 않는 확장자 (400 Bad Request)
  @Test
  @DisplayName("Post /api/admin/products - 지원하지 않는 확장자 업로드 시 400 반환")
  void createProduct_invalidExtension_400() throws Exception {
    ProductCreateRequest request = ProductCreateRequest.builder()
        .name("딸기")
        .price(1000)
        .category(FOOD)
        .stock(10)
        .build();

    MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json",
        "content".getBytes());
    MockMultipartFile exeFile = new MockMultipartFile("image", "wrong.exe",
        "application/octet-stream", "content".getBytes());

    given(productService.register(any(), any())).willThrow(
        new BusinessException(ProductErrorCode.PRODUCT_INVALID_IMAGE));

    // when & then
    mockMvc.perform(multipart("/api/admin/products")
            .file(requestPart)
            .file(exeFile))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.result.code").value(400));
  }

  // 수정 실패 - 수정 시 가격을 음수로 변경 시도
  @Test
  @DisplayName("PUT /api/admin/products/{id} - 가격을 음수로 수정 시 400 반환")
  void updateProduct_invalidPrice_400() throws Exception {
    // given
    Long productId = 1L;
    ProductUpdateRequest request = ProductUpdateRequest.builder()
        .name("딸기")
        .price(-1000)
        .category(FOOD)
        .status(ProductStatus.FOR_SALE)
        .build();

    MockMultipartFile requestPart = new MockMultipartFile(
        "request",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    // when & then
    mockMvc.perform(multipart(HttpMethod.PUT, "/api/admin/products/{id}", productId)
            .file(requestPart))
        .andExpect(status().isBadRequest());
  }

  // 삭제 실패 - 이미 삭제되었거나 존재하지 않는 상품 삭제 시도
  @Test
  @DisplayName("PATCH /api/admin/products/{id} - 존재하지 않는 상품 삭제 시 404 반환")
  void deleteProduct_notFound_404() throws Exception {
    // given
    Long invalidId = 999L;
    willThrow(new com.shoppingmall.ecommerceapi.common.exception.BusinessException(
        com.shoppingmall.ecommerceapi.domain.product.exception.ProductErrorCode.PRODUCT_NOT_FOUND))
        .given(productService).deleteProduct(invalidId);

    // when & then
    mockMvc.perform(patch("/api/admin/products/{id}", invalidId))
        .andExpect(status().isNotFound());
  }
}