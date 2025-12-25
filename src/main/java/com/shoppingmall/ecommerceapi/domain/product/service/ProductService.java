package com.shoppingmall.ecommerceapi.domain.product.service;

import com.shoppingmall.ecommerceapi.common.exception.BusinessException;
import com.shoppingmall.ecommerceapi.common.response.PageRequestDTO;
import com.shoppingmall.ecommerceapi.common.response.PageResponse;
import com.shoppingmall.ecommerceapi.domain.product.converter.ProductConverter;
import com.shoppingmall.ecommerceapi.domain.product.dto.ProductCreateRequest;
import com.shoppingmall.ecommerceapi.domain.product.dto.ProductResponse;
import com.shoppingmall.ecommerceapi.domain.product.dto.ProductUpdateRequest;
import com.shoppingmall.ecommerceapi.domain.product.entity.Product;
import com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductCategory;
import com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductStatus;
import com.shoppingmall.ecommerceapi.domain.product.exception.ProductErrorCode;
import com.shoppingmall.ecommerceapi.domain.product.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProductService {

  private final ProductRepository productRepository;
  private final ProductConverter productConverter;

  // 상품 등록
  @Transactional
  public ProductResponse register(ProductCreateRequest request) {
    // 이미지 유효성 검사
    String finalImgSrc = (request.getImgSrc() == null || request.getImgSrc().isBlank())
        ? "none.png" : request.getImgSrc();
    if (!isValidImageExtension(finalImgSrc)) {
      throw new BusinessException(ProductErrorCode.PRODUCT_INVALID_IMAGE);
    }

    // 재고에 따른 판매 상태
    ProductStatus status =
        (request.getStock() > 0) ? ProductStatus.FOR_SALE : ProductStatus.SOLD_OUT;

    Product product = productConverter.toEntity(request, finalImgSrc, status);

    Product savedProduct = productRepository.save(product);
    return productConverter.toResponse(savedProduct);
  }

  // 상품 수정
  @Transactional
  public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
    // 기존 상품 조회
    Product product = productRepository.findById(id)
        .filter(p -> p.getDeletedAt() == null)
        .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_UPDATE_NOT_FOUND));

    // 이미지 유효성 검사
    String finalImgSrc = (request.getImgSrc() == null || request.getImgSrc().isBlank())
        ? "none.png" : request.getImgSrc();
    if (!isValidImageExtension(finalImgSrc)) {
      throw new BusinessException(ProductErrorCode.PRODUCT_INVALID_IMAGE);
    }

    // 재고 0일때 판매중 설정 불가
    if (request.getStock() == 0 && request.getStatus() == ProductStatus.FOR_SALE) {
      throw new BusinessException(ProductErrorCode.PRODUCT_STATUS_CONFLICT);
    }

    // 더티 체킹
    product.update(
        request.getName(),
        request.getDescription(),
        request.getPrice(),
        request.getCategory(),
        request.getStatus(),
        request.getStock(),
        finalImgSrc
    );

    return productConverter.toResponse(product);
  }

  // 상품 단건 조회
  @Transactional(readOnly = true)
  public ProductResponse getProduct(Long id) {
    return productRepository.findById(id)
        .filter(product -> product.getDeletedAt() == null)
        .map(productConverter::toResponse)
        .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));
  }

  // 상품 전체 조회
  @Transactional(readOnly = true)
  public PageResponse<ProductResponse> getProducts(ProductCategory category, PageRequestDTO req) {
    // 페이지 번호, 사이즈
    if (req.getPage() < 0) {
      throw new BusinessException(ProductErrorCode.PRODUCT_INVALID_PAGE);
    }
    if (req.getSize() <= 0) {
      throw new BusinessException(ProductErrorCode.PRODUCT_INVALID_PAGE_SIZE);
    }
    // 정렬 기준
    Pageable pageable;
    try {
      pageable = req.toPageable();
    } catch (Exception e) {
      throw new BusinessException(ProductErrorCode.PRODUCT_INVALID_SORT);
    }
    // 캍[고리
    Page<Product> productPage;
    if (category != null) {
      productPage = productRepository.findAllByCategoryAndDeleteAtIsNull(category, pageable);
    } else {
      productPage = productRepository.findAllByDeleteAtIsNull(pageable);
    }

    List<ProductResponse> content = productPage.getContent().stream()
        .map(productConverter::toResponse)
        .toList();

    return PageResponse.of(productPage, content, req.getSort());
  }

  // 이미지 파일 유효성 검사
  private boolean isValidImageExtension(String fileName) {
    if ("none.png".equals(fileName)) {
      return true;
    }
    String lowercase = fileName.toLowerCase();
    return lowercase.endsWith(".jpg") || lowercase.endsWith(".png") || lowercase.endsWith(".jpeg");
  }

  // 헬퍼 메서드
  @Transactional(readOnly = true)
  public Product findProductEntityById(Long id) {
    return productRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));
  }
}
