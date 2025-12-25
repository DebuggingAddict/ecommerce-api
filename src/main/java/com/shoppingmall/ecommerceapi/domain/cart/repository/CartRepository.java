package com.shoppingmall.ecommerceapi.domain.cart.repository;

import com.shoppingmall.ecommerceapi.domain.cart.entity.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

  Optional<Cart> findByUserId(Long userId);

  boolean existsByUserId(Long userId);
}
