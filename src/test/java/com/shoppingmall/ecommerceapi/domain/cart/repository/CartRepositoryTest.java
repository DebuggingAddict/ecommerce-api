package com.shoppingmall.ecommerceapi.domain.cart.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoppingmall.ecommerceapi.config.jpa.JpaAuditingConfig;
import com.shoppingmall.ecommerceapi.domain.cart.entity.Cart;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
class CartRepositoryTest {

  private final CartRepository cartRepository;

  @Autowired
  CartRepositoryTest(CartRepository cartRepository) {
    this.cartRepository = cartRepository;
  }

  static Long TEST_USER_ID = 60L;

  @Test
  @DisplayName("유저ID로 카트 찾기")
  void testFindCartByUserId() {
    // Given
    Long userId = TEST_USER_ID;
    Cart saved = cartRepository.save(Cart.builder()
        .userId(userId)
        .build());

    // When
    Optional<Cart> result = cartRepository.findByUserId(userId);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(saved.getId());
    assertThat(result.get().getUserId()).isEqualTo(userId);
  }

  @Test
  @DisplayName("유저ID로 카트 존재 여부 확인")
  void testExistsByUserId() {
    // Given
    Long userId = TEST_USER_ID;
    Long notExistsUserId = 999L;

    cartRepository.save(Cart.builder()
        .userId(userId)
        .build());

    // When
    boolean exists = cartRepository.existsByUserId(userId);
    boolean notExists = cartRepository.existsByUserId(notExistsUserId);

    // Then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }
}
