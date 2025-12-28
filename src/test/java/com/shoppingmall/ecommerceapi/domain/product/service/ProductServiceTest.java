package com.shoppingmall.ecommerceapi.domain.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.shoppingmall.ecommerceapi.common.exception.BusinessException;
import com.shoppingmall.ecommerceapi.common.response.PageRequestDTO;
import com.shoppingmall.ecommerceapi.domain.order.repository.OrderItemRepository;
import com.shoppingmall.ecommerceapi.domain.product.converter.ProductConverter;
import com.shoppingmall.ecommerceapi.domain.product.dto.ProductCreateRequest;
import com.shoppingmall.ecommerceapi.domain.product.dto.ProductUpdateRequest;
import com.shoppingmall.ecommerceapi.domain.product.entity.Product;
import com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductStatus;
import com.shoppingmall.ecommerceapi.domain.product.exception.ProductErrorCode;
import com.shoppingmall.ecommerceapi.domain.product.repository.ProductRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

  @InjectMocks
  private ProductService productService;

  @Mock
  private ProductRepository productRepository;

  @Mock
  private ProductConverter productConverter;

  @Mock
  private OrderItemRepository orderItemRepository;

  // 상품 등록 - 잘못된 이미지 확장자 예외 처리 테스트
  @Test
  @DisplayName("상품 등록 - 잘못된 이미지 확장자 예외 처리")
  void register_invalidImage_throwsException() {
    // given
    ProductCreateRequest request = ProductCreateRequest.builder()
        .name("딸기")
        .imgSrc("goorm.exe")
        .stock(10)
        .price(1000)
        .build();

    // when & then
    BusinessException exception = assertThrows(BusinessException.class, () -> {
      productService.register(request);
    });
    assertEquals(ProductErrorCode.PRODUCT_INVALID_IMAGE, exception.getCode());
  }

  // 상품 등록 - 재고에 따른 판매상태 테스트
  @Test
  @DisplayName("상품 등록 - 재고가 0이면 자동으로 SOLD_OUT 상태로 등록")
  void register_zeroStock_setsSoldOut() {
    // given
    ProductCreateRequest request = ProductCreateRequest.builder()
        .name("품절딸기").stock(0).imgSrc("apple.png").build();

    given(productConverter.toEntity(any(), any(), eq(ProductStatus.SOLD_OUT)))
        .willReturn(new Product());

    // when & then
    productService.register(request);
    verify(productConverter).toEntity(any(), any(), eq(ProductStatus.SOLD_OUT));
  }

  // 상품 수정 - 재고가 0일때 판매중 설정 불가 테스트
  @Test
  @DisplayName("상품 수정 - 재고가 0일때 판매중으로 변경시 예외처리")
  void updateProduct_stockZeroStatusConflict_throwsException() {
    // given
    Long productId = 1L;
    Product product = Product.builder().id(productId).stock(10).build();
    ProductUpdateRequest request = ProductUpdateRequest.builder()
        .stock(0)
        .status(ProductStatus.FOR_SALE)
        .imgSrc("test.png")
        .build();

    given(productRepository.findById(productId)).willReturn(Optional.of(product));

    // when & then
    BusinessException exception = assertThrows(BusinessException.class, () -> {
      productService.updateProduct(productId, request);
    });
    assertEquals(ProductErrorCode.PRODUCT_STATUS_CONFLICT, exception.getCode());
  }

  // 상품 삭제 - 삭제된 상품 수정 테스트
  @Test
  @DisplayName("상품 삭제 - 이미 삭제된 상품은 수정할 수 없음")
  void updateProduct_alreadyDeleted_throwsException() {
    // given
    Long productId = 1L;
    Product deletedProduct = Product.builder()
        .id(productId)
        .deletedAt(java.time.LocalDateTime.now())
        .build();

    given(productRepository.findById(productId)).willReturn(Optional.of(deletedProduct));

    // when & then
    BusinessException exception = assertThrows(BusinessException.class, () -> {
      productService.updateProduct(productId, any(ProductUpdateRequest.class));
    });
    assertEquals(ProductErrorCode.PRODUCT_UPDATE_NOT_FOUND, exception.getCode());
  }

  // 상품 삭제 - 주문 내역 존재 하는 상품 삭제 테스트
  @Test
  @DisplayName("상품 삭제 - 주문 내역이 존재하는 상품은 삭제할 수 없음")
  void deleteProduct_hasOrderHistory_throwsException() {
    // given
    Long productId = 1L;
    Product product = Product.builder().id(productId).build();

    given(productRepository.findById(productId)).willReturn(Optional.of(product));
    given(orderItemRepository.existsByProductId(productId)).willReturn(true);

    // when & then
    BusinessException exception = assertThrows(BusinessException.class, () -> {
      productService.deleteProduct(productId);
    });
    assertEquals(ProductErrorCode.PRODUCT_DELETE_FAILED, exception.getCode());
  }

  // 상품 전체 조회 - 페이지 번호가 음수일 경우 테스트
  @Test
  @DisplayName("전체 조회 - 페이지 번호가 음수일 때 예외 처리")
  void getProducts_negativePage_throwsException() {
    // given
    PageRequestDTO request = new PageRequestDTO();
    request.setPage(-1);

    // when & then
    BusinessException exception = assertThrows(BusinessException.class, () -> {
      productService.getProducts(null, request);
    });
    assertEquals(ProductErrorCode.PRODUCT_INVALID_PAGE, exception.getCode());
  }

  // 상품 전체 조회 - 잘못된 상태값 들어오면 null 반환하여 필터 무시
  @Test
  @DisplayName("전체 조회 - 잘못된 상태값이 들어오면 null을 반환하여 필터를 무시함")
  void getProducts_invalidSortType_returnsAllProducts() {
    // given
    PageRequestDTO request = PageRequestDTO.builder()
        .page(0).size(10).sortType("INVALID_ENUM_STRING").build();

    given(productRepository.findAllByDeletedAtIsNull(any())).willReturn(
        org.springframework.data.domain.Page.empty());

    // when & then
    productService.getProducts(null, request);
    verify(productRepository).findAllByDeletedAtIsNull(any());
  }

  // 상품 단건 조회 - 삭제된 상품 조회 테스트
  @Test
  @DisplayName("단건 조회 - 이미 삭제된 상품은 조회할 수 없음")
  void getProduct_alreadyDeleted_throwsException() {
    // given
    Long productId = 1L;
    Product deletedProduct = Product.builder()
        .id(productId)
        .deletedAt(java.time.LocalDateTime.now())
        .build();

    given(productRepository.findById(productId)).willReturn(Optional.of(deletedProduct));

    // when & then
    BusinessException exception = assertThrows(BusinessException.class, () -> {
      productService.getProduct(productId);
    });
    assertEquals(ProductErrorCode.PRODUCT_NOT_FOUND, exception.getCode());
  }
}
