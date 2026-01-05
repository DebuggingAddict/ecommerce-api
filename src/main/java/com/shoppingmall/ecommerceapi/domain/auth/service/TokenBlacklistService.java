package com.shoppingmall.ecommerceapi.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Access Token 블랙리스트 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    /**
     * 토큰을 블랙리스트에 추가
     * @param token 블랙리스트에 추가할 Access Token
     * @param expirationMillis 토큰 남은 유효시간 (밀리초)
     */
    public void addToBlacklist(String token, long expirationMillis) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue()
                .set(key, "blacklisted", expirationMillis, TimeUnit.MILLISECONDS);
        log.info("블랙리스트에 토큰 추가: {}", maskToken(token));
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     * @param token 확인할 Access Token
     * @return 블랙리스트에 있으면 true
     */
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return redisTemplate.hasKey(key);
    }

    /**
     * 토큰 마스킹 (로그용)
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 10) + "...";
    }
}
