package com.shoppingmall.ecommerceapi.domain.product.repository;

import com.shoppingmall.ecommerceapi.domain.product.entity.Product;
import com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

  // 삭제되지 않은 전체 상품 조회
  Page<Product> findAllByDeleteAtIsNull(Pageable pageable);

  // 삭제되지 않은 특정 카테고리 상품 조회
  Page<Product> findAllByCategoryAndDeleteAtIsNull(ProductCategory productCategory,
      Pageable pageable);
}
