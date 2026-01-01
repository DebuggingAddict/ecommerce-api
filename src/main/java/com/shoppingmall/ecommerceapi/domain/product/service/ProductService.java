package com.shoppingmall.ecommerceapi.domain.product.service;

import com.shoppingmall.ecommerceapi.common.exception.BusinessException;
import com.shoppingmall.ecommerceapi.common.infra.S3Service;
import com.shoppingmall.ecommerceapi.common.response.PageRequestDTO;
import com.shoppingmall.ecommerceapi.common.response.PageResponse;
import com.shoppingmall.ecommerceapi.domain.order.repository.OrderItemRepository;
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
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class ProductService {

  private final ProductRepository productRepository;
  private final ProductConverter productConverter;
  private final OrderItemRepository orderItemRepository;
  private final S3Service s3Service;

  // 상품 등록
  @Transactional
  public ProductResponse register(ProductCreateRequest request, MultipartFile image) {
    String finalImgSrc = "none.png";

    // 이미지가 넘어왔을 경우에만 S3 업로드 실행
    if (image != null && !image.isEmpty()) {
      finalImgSrc = s3Service.uploadFile(image);
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
    // 수정할 상품 조회
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

  // 상품 삭제
  @Transactional
  public void deleteProduct(Long id) {
    // 삭제할 상품 조회
    Product product = productRepository.findById(id)
        .filter(p -> p.getDeletedAt() == null)
        .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_DELETE_NOT_FOUND));

    // 주문 내역 존재 확인
    if (orderItemRepository.existsByProductId(id)) {
      throw new BusinessException(ProductErrorCode.PRODUCT_DELETE_FAILED);
    }

    // 더티체킹
    product.delete();
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

    // sortType을 ProductStatus로 변환 (Product 도메인에서만 처리)
    ProductStatus status = convertToProductStatus(req.getSortType());

    // 카테고리 + 상태 조합 필터링
    Page<Product> productPage;
    if (category != null && status != null) {
      // 카테고리 + 상태 모두 지정
      productPage = productRepository.findAllByCategoryAndStatusAndDeletedAtIsNull(category, status,
          pageable);
    } else if (category != null) {
      // 카테고리만 지정
      productPage = productRepository.findAllByCategoryAndDeletedAtIsNull(category, pageable);
    } else if (status != null) {
      // 상태만 지정
      productPage = productRepository.findAllByStatusAndDeletedAtIsNull(status, pageable);
    } else {
      // 필터 없음
      productPage = productRepository.findAllByDeletedAtIsNull(pageable);
    }

    List<ProductResponse> content = productPage.getContent().stream()
        .map(productConverter::toResponse)
        .toList();

    return PageResponse.of(productPage, content, req.getSort());
  }

  /**
   * sortType 문자열을 ProductStatus로 변환 잘못된 값이거나 null이면 null 반환 (필터 무시)
   */
  private ProductStatus convertToProductStatus(String sortType) {
    if (sortType == null || sortType.isEmpty()) {
      return null;
    }
    try {
      // 공백, 큰따옴표, 작은따옴표 제거
      String cleaned = sortType.trim().replace("\"", "").replace("'", "");
      ProductStatus status = ProductStatus.valueOf(cleaned.toUpperCase());
      return status;
    } catch (IllegalArgumentException e) {
      return null;
    }
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