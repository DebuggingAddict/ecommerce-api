package com.shoppingmall.ecommerceapi.domain.auth.repository;

import java.time.Duration;
import java.util.Optional;

public interface RefreshTokenStore {

    /**
     * Refresh Token 저장
     */
    void save(Long userId, String refreshToken, String deviceInfo, Duration expiration);

    /**
     * Refresh Token으로 userId 조회
     */
    Optional<Long> findUserIdByToken(String refreshToken);

    /**
     * Refresh Token 삭제
     */
    void delete(String refreshToken);

    /**
     * 특정 사용자의 모든 Refresh Token 삭제
     */
    void deleteAllByUserId(Long userId);

    /**
     * Refresh Token 존재 여부 확인
     */
    boolean exists(String refreshToken);
}
