package com.shoppingmall.ecommerceapi.domain.user.repository;


import com.shoppingmall.ecommerceapi.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  Optional<User> findByUsername(String username);
}

