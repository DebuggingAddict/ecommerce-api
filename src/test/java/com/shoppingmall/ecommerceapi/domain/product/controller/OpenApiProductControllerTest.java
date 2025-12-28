package com.shoppingmall.ecommerceapi.domain.product.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.ecommerceapi.common.response.PageRequestDTO;
import com.shoppingmall.ecommerceapi.common.response.PageResponse;
import com.shoppingmall.ecommerceapi.domain.product.dto.ProductResponse;
import com.shoppingmall.ecommerceapi.domain.product.service.ProductService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
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
  ProductService productService;

  // 샹품 단건 조회 테스트
  @Test
  @DisplayName("GET /open-api/products/{id} - 상품 단건 조회 성공")
  void getProduct_success() throws Exception {
    // given
    Long productId = 1L;
    ProductResponse response = ProductResponse.builder()
        .id(productId)
        .name("딸기")
        .price(15000)
        .category("FOOD")
        .stock(50)
        .status("FOR_SALE")
        .description("맛있는 딸기입니다!")
        .imgSrc("none.png")
        .build();

    given(productService.getProduct(productId)).willReturn(response);

    mockMvc.perform(get("/open-api/products/{id}", productId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.body.name").value("딸기"))
        .andExpect(jsonPath("$.body.price").value("15000"))
        .andExpect(jsonPath("$.body.category").value("FOOD"))
        .andExpect(jsonPath("$.body.stock").value("50"))
        .andExpect(jsonPath("$.body.status").value("FOR_SALE"))
        .andExpect(jsonPath("$.body.description").value("맛있는 딸기입니다!"))
        .andExpect(jsonPath("$.body.imgSrc").value("none.png"));
  }


  // 상품 전체 조회 테스트
  @Test
  @DisplayName("Get /open-api/products - 전체 상품 조회 성공")
  void getProducts_success() throws Exception {
    // given
    List<ProductResponse> emptyList = List.of();
    PageResponse<ProductResponse> emptyPageResponse = PageResponse.of(
        Page.empty(),
        emptyList,
        "FOR_SALE"
    );

    given(productService.getProducts(any(), any(PageRequestDTO.class))).willReturn(
        emptyPageResponse);

    mockMvc.perform(get("/open-api/products")
            .param("page", "0")
            .param("size", "10")
            .param("sort", "price")
            .param("category", "FOOD"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.body.content").isArray())
        .andExpect(jsonPath("$.body.content").isEmpty())
        .andExpect(jsonPath("$.body.pageInfo").exists())
        .andExpect(jsonPath("$.body.pageInfo.currentSort").value("FOR_SALE"))
        .andExpect(jsonPath("$.body.pageInfo.currentPage").value(0));
  }

  // 조회 실패 - 존재하지 않는 상품 단건 조회 (404 Not Found)
  @Test
  @DisplayName("GET /open-api/products/{id} - 존재하지 않는 ID 조회 시 404 반환")
  void getProduct_notFound_404() throws Exception {
    // given
    Long invalidId = 9999L;
    given(productService.getProduct(invalidId))
        .willThrow(new com.shoppingmall.ecommerceapi.common.exception.BusinessException(
            com.shoppingmall.ecommerceapi.domain.product.exception.ProductErrorCode.PRODUCT_NOT_FOUND));

    // when & then
    mockMvc.perform(get("/open-api/products/{id}", invalidId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.result.message").exists());
  }

  // 페이징 실패 - 잘못된 페이지 번호 요청 (400 Bad Request)
  @Test
  @DisplayName("GET /open-api/products - 페이지 번호가 음수일 때 400 반환")
  void getProducts_invalidPage_400() throws Exception {
    // given
    given(productService.getProducts(any(), any(PageRequestDTO.class)))
        .willThrow(new com.shoppingmall.ecommerceapi.common.exception.BusinessException(
            com.shoppingmall.ecommerceapi.domain.product.exception.ProductErrorCode.PRODUCT_INVALID_PAGE));

    // when & then
    mockMvc.perform(get("/open-api/products")
            .param("page", "-1"))
        .andExpect(status().isBadRequest());
  }

  // 파라미터 실패 - 정의되지 않은 카테고리 값 입력 (400 Bad Request)
  @Test
  @DisplayName("GET /open-api/products - 존재하지 않는 카테고리 이름 입력 시 400 반환")
  void getProducts_invalidCategory_400() throws Exception {
    mockMvc.perform(get("/open-api/products")
            .param("category", "ELECTRONICS"))
        .andExpect(status().isBadRequest());
  }

  // 정렬 실패 - 잘못된 정렬 파라미터 형식 (400 Bad Request)
  @Test
  @DisplayName("GET /open-api/products - 잘못된 정렬 기준 요청 시 400 반환")
  void getProducts_invalidSort_400() throws Exception {
    // given
    given(productService.getProducts(any(), any(PageRequestDTO.class)))
        .willThrow(new com.shoppingmall.ecommerceapi.common.exception.BusinessException(
            com.shoppingmall.ecommerceapi.domain.product.exception.ProductErrorCode.PRODUCT_INVALID_SORT));

    // when & then
    mockMvc.perform(get("/open-api/products")
            .param("sort", "unknwonField,asc"))
        .andExpect(status().isBadRequest());
  }
}
