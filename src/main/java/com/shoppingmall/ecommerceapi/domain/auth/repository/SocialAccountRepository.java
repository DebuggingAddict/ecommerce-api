package com.shoppingmall.ecommerceapi.domain.auth.repository;

import com.shoppingmall.ecommerceapi.domain.auth.entity.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    /**
     * OAuth2 로그인 시 기존 사용자 찾기
     */
    Optional<SocialAccount> findByProviderAndProviderUserId(String provider, String providerUserId);

    /**
     * 특정 사용자의 연결된 소셜 계정 목록 (멀티 소셜 연결 시)
     */
    List<SocialAccount> findByUserId(Long userId);

    /**
     * 사용자의 특정 Provider 계정 존재 여부
     */
    boolean existsByUserIdAndProvider(Long userId, String provider);
}
