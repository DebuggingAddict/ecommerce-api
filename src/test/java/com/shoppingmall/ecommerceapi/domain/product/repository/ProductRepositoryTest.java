package com.shoppingmall.ecommerceapi.domain.product.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoppingmall.ecommerceapi.domain.product.entity.Product;
import com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductCategory;
import com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@DataJpaTest
@Import(ProductRepositoryTest.TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {

  @TestConfiguration
  @EnableJpaAuditing
  static class TestConfig {

  }

  @Autowired
  private ProductRepository productRepository;

  private Pageable pageable;

  private String foodName;
  private String soldOutName;
  private String fashionName;

  @BeforeEach
  void setUp() {
    pageable = PageRequest.of(0, 100);

    foodName = "유기농 사과_" + UUID.randomUUID().toString().substring(0, 8);
    soldOutName = "품절된 우유_" + UUID.randomUUID().toString().substring(0, 8);
    fashionName = "청바지_" + UUID.randomUUID().toString().substring(0, 8);

    productRepository.save(Product.builder()
        .name(foodName).price(5000).stock(100)
        .category(ProductCategory.FOOD).status(ProductStatus.FOR_SALE)
        .build());

    productRepository.save(Product.builder()
        .name(soldOutName).price(3000).stock(0)
        .category(ProductCategory.FOOD).status(ProductStatus.SOLD_OUT)
        .build());

    productRepository.save(Product.builder()
        .name(fashionName).price(35000).stock(50)
        .category(ProductCategory.FASHION).status(ProductStatus.FOR_SALE)
        .build());

    productRepository.save(Product.builder()
        .name("삭제된 티셔츠_TEST").price(10000).stock(10)
        .category(ProductCategory.FASHION).status(ProductStatus.STOP_SALE)
        .deletedAt(LocalDateTime.now())
        .build());
  }

  @Test
  @DisplayName("삭제되지 않은 전체 상품 조회 - 삭제된 상품은 포함되지 않아야 함")
  void findAllByDeletedAtIsNull_Success() {
    // when
    Page<Product> result = productRepository.findAllByDeletedAtIsNull(pageable);

    // then
    assertThat(result.getContent()).extracting("name")
        .doesNotContain("삭제된 티셔츠_TEST")
        .contains(foodName, soldOutName, fashionName);
  }

  @Test
  @DisplayName("특정 카테고리 상품 조회 - 해당 카테고리이며 삭제되지 않은 상품만 조회")
  void findAllByCategoryAndDeletedAtIsNull_Success() {
    // when
    Page<Product> result = productRepository.findAllByCategoryAndDeletedAtIsNull(
        ProductCategory.FOOD, pageable);

    // then
    assertThat(result.getContent()).extracting("category").containsOnly(ProductCategory.FOOD);
    assertThat(result.getContent()).extracting("name").contains(foodName, soldOutName);
  }

  @Test
  @DisplayName("특정 상태 상품 조회 - 해당 상태이며 삭제되지 않은 상품만 조회")
  void findAllByStatusAndDeletedAtIsNull_Success() {
    // when
    Page<Product> result = productRepository.findAllByStatusAndDeletedAtIsNull(
        ProductStatus.SOLD_OUT, pageable);

    // then
    assertThat(result.getContent()).extracting("name").contains(soldOutName);
    assertThat(result.getContent()).extracting("status").containsOnly(ProductStatus.SOLD_OUT);
  }

  @Test
  @DisplayName("카테고리와 상태 조합 조회 - 두 조건이 일치하며 삭제되지 않은 상품만 조회")
  void findAllByCategoryAndStatusAndDeletedAtIsNull_Success() {
    // when
    Page<Product> result = productRepository.findAllByCategoryAndStatusAndDeletedAtIsNull(
        ProductCategory.FASHION, ProductStatus.FOR_SALE, pageable);

    // then
    assertThat(result.getContent()).extracting("name").contains(fashionName);
  }

  @Test
  @DisplayName("존재하지 않는 카테고리나 상태로 조회 시 빈 페이지 반환")
  void findEmptyPage_WhenConditionNotMatch() {
    // when
    Page<Product> result = productRepository.findAllByCategoryAndStatusAndDeletedAtIsNull(
        ProductCategory.BABY, ProductStatus.SOLD_OUT, pageable);

    // then
    assertThat(result.getContent()).extracting("name")
        .doesNotContain(foodName, soldOutName, fashionName);
  }
}