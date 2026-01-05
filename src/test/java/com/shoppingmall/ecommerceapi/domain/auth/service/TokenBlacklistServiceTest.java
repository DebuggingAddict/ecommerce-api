package com.shoppingmall.ecommerceapi.domain.auth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;

    @Test
    @DisplayName("addToBlacklist: prefix가 붙은 key로 value/TTL을 함께 저장한다")
    void addToBlacklist_setsValueWithTtl() {
        // given
        String token = "abcdefghijklmnopqrstuvwxyz.0123456789";
        long expirationMillis = 1234L;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        tokenBlacklistService.addToBlacklist(token, expirationMillis);

        // then
        String expectedKey = "blacklist:token:" + token;

        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1))
                .set(expectedKey, "blacklisted", expirationMillis, TimeUnit.MILLISECONDS); // ValueOperations.set 오버로드
        verifyNoMoreInteractions(redisTemplate, valueOperations);
    }

    @Test
    @DisplayName("isBlacklisted: hasKey가 true면 true를 반환한다")
    void isBlacklisted_returnsTrue() {
        // given
        String token = "token-123";
        String expectedKey = "blacklist:token:" + token;

        when(redisTemplate.hasKey(expectedKey)).thenReturn(true);

        // when
        boolean result = tokenBlacklistService.isBlacklisted(token);

        // then
        assertThat(result).isTrue();
        verify(redisTemplate, times(1)).hasKey(expectedKey);
        verifyNoMoreInteractions(redisTemplate);
    }

    @Test
    @DisplayName("isBlacklisted: hasKey가 false면 false를 반환한다")
    void isBlacklisted_returnsFalse() {
        // given
        String token = "token-456";
        String expectedKey = "blacklist:token:" + token;

        when(redisTemplate.hasKey(expectedKey)).thenReturn(false);

        // when
        boolean result = tokenBlacklistService.isBlacklisted(token);

        // then
        assertThat(result).isFalse();
        verify(redisTemplate, times(1)).hasKey(expectedKey);
        verifyNoMoreInteractions(redisTemplate);
    }
}
