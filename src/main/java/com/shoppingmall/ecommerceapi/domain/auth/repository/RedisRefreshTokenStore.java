package com.shoppingmall.ecommerceapi.domain.auth.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String TOKEN_PREFIX = "refresh_token:";
    private static final String USER_TOKEN_PREFIX = "user_tokens:";

    @Override
    public void save(Long userId, String refreshToken, String deviceInfo, Duration expiration) {
        String tokenKey = TOKEN_PREFIX + refreshToken;
        String userTokenKey = USER_TOKEN_PREFIX + userId;

        // refresh_token:{token} -> userId 저장
        redisTemplate.opsForValue().set(tokenKey, userId.toString(), expiration);

        // user_tokens:{userId} -> Set<token> 저장 (멀티 디바이스)
        redisTemplate.opsForSet().add(userTokenKey, refreshToken);
        redisTemplate.expire(userTokenKey, expiration);

        log.debug("Refresh Token 저장: userId={}, token={}, device={}", userId, refreshToken.substring(0, 10) + "...", deviceInfo);
    }

    @Override
    public Optional<Long> findUserIdByToken(String refreshToken) {
        String tokenKey = TOKEN_PREFIX + refreshToken;
        String userIdStr = redisTemplate.opsForValue().get(tokenKey);

        if (userIdStr != null) {
            return Optional.of(Long.parseLong(userIdStr));
        }
        return Optional.empty();
    }

    @Override
    public void delete(String refreshToken) {
        String tokenKey = TOKEN_PREFIX + refreshToken;

        // userId 조회 후 user_tokens에서도 제거
        String userIdStr = redisTemplate.opsForValue().get(tokenKey);
        if (userIdStr != null) {
            Long userId = Long.parseLong(userIdStr);
            String userTokenKey = USER_TOKEN_PREFIX + userId;
            redisTemplate.opsForSet().remove(userTokenKey, refreshToken);
        }

        // 토큰 삭제
        redisTemplate.delete(tokenKey);
        log.debug("Refresh Token 삭제: token={}", refreshToken.substring(0, 10) + "...");
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        String userTokenKey = USER_TOKEN_PREFIX + userId;

        // 해당 사용자의 모든 토큰 조회
        Set<String> tokens = redisTemplate.opsForSet().members(userTokenKey);

        if (tokens != null && !tokens.isEmpty()) {
            // 각 토큰 삭제
            tokens.forEach(token -> {
                String tokenKey = TOKEN_PREFIX + token;
                redisTemplate.delete(tokenKey);
            });

            // user_tokens 삭제
            redisTemplate.delete(userTokenKey);
            log.debug("사용자의 모든 Refresh Token 삭제: userId={}, count={}", userId, tokens.size());
        }
    }

    @Override
    public boolean exists(String refreshToken) {
        String tokenKey = TOKEN_PREFIX + refreshToken;
        return redisTemplate.hasKey(tokenKey);
    }
}
