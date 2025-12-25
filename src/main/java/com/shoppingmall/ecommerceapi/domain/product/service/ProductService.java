package com.shoppingmall.ecommerceapi.domain.product.service;

import com.shoppingmall.ecommerceapi.common.exception.BusinessException;
import com.shoppingmall.ecommerceapi.domain.product.converter.ProductConverter;
import com.shoppingmall.ecommerceapi.domain.product.dto.ProductCreateRequest;
import com.shoppingmall.ecommerceapi.domain.product.dto.ProductResponse;
import com.shoppingmall.ecommerceapi.domain.product.entity.Product;
import com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductStatus;
import com.shoppingmall.ecommerceapi.domain.product.exception.ProductErrorCode;
import com.shoppingmall.ecommerceapi.domain.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProductService {

  private final ProductRepository productRepository;
  private final ProductConverter productConverter;

  @Transactional
  public ProductResponse register(ProductCreateRequest request) {
    String finalImgSrc = (request.getImgSrc() == null) ? "none.png" : request.getImgSrc();
    if (!isValidImageExtension(finalImgSrc)) {
      throw new BusinessException(ProductErrorCode.PRODUCT_INVALID_IMAGE, "허용되지 않는 이미지 확장자입니다.");
    }

    ProductStatus status =
        (request.getStock() > 0) ? ProductStatus.FOR_SALE : ProductStatus.SOLD_OUT;

    Product product = productConverter.toEntity(request, finalImgSrc, status);

    Product savedProduct = productRepository.save(product);
    return productConverter.toResponse(savedProduct);
  }

  private boolean isValidImageExtension(String fileName) {
    if ("none.png".equals(fileName)) {
      return true;
    }
    String lowercase = fileName.toLowerCase();
    return lowercase.endsWith(".jpg") || lowercase.endsWith(".png") || lowercase.endsWith(".jpeg");
  }

}
