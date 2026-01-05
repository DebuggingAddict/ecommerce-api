package com.shoppingmall.ecommerceapi.domain.auth.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisRefreshTokenStoreTest {

    @Mock RedisTemplate<String, String> redisTemplate;
    @Mock ValueOperations<String, String> valueOps;
    @Mock SetOperations<String, String> setOps;

    @InjectMocks RedisRefreshTokenStore store;

    @Test
    @DisplayName("save(): refresh_token:{token}에 userId 저장 + user_tokens:{userId} Set에 token 추가 + TTL 설정")
    void save_shouldStoreTokenAndUserMapping_andSetExpire() {
        // given
        Long userId = 1L;
        String token = "rft_abcdefghijklmnopqrstuvwxyz"; // 10자 이상
        String deviceInfo = "iPhone";
        Duration expiration = Duration.ofDays(7);

        // 필요한 ops만 stubbing
        doReturn(valueOps).when(redisTemplate).opsForValue(); // RedisTemplate ops mocking 패턴
        doReturn(setOps).when(redisTemplate).opsForSet();

        // when
        store.save(userId, token, deviceInfo, expiration);

        // then
        String tokenKey = "refresh_token:" + token;
        String userTokenKey = "user_tokens:" + userId;

        verify(valueOps).set(tokenKey, userId.toString(), expiration);
        verify(setOps).add(userTokenKey, token);
        verify(redisTemplate).expire(userTokenKey, expiration);

        verifyNoMoreInteractions(valueOps, setOps);
    }

    @Test
    @DisplayName("findUserIdByToken(): 토큰이 존재하면 userId 반환")
    void findUserIdByToken_shouldReturnUserId_whenTokenExists() {
        // given
        String token = "rft_token_12345";
        String tokenKey = "refresh_token:" + token;

        doReturn(valueOps).when(redisTemplate).opsForValue(); // [web:54]
        when(valueOps.get(tokenKey)).thenReturn("10");

        // when
        Optional<Long> result = store.findUserIdByToken(token);

        // then
        assertThat(result).contains(10L);
        verify(valueOps).get(tokenKey);
        verifyNoMoreInteractions(valueOps);
        verifyNoInteractions(setOps);
    }

    @Test
    @DisplayName("findUserIdByToken(): 토큰이 없으면 empty")
    void findUserIdByToken_shouldReturnEmpty_whenTokenNotExists() {
        // given
        String token = "rft_token_12345";
        String tokenKey = "refresh_token:" + token;

        doReturn(valueOps).when(redisTemplate).opsForValue(); // [web:54]
        when(valueOps.get(tokenKey)).thenReturn(null);

        // when
        Optional<Long> result = store.findUserIdByToken(token);

        // then
        assertThat(result).isEmpty();
        verify(valueOps).get(tokenKey);
        verifyNoMoreInteractions(valueOps);
        verifyNoInteractions(setOps);
    }

    @Test
    @DisplayName("delete(): user_tokens:{userId}에서 token 제거 후 refresh_token:{token} 키 삭제")
    void delete_shouldRemoveFromUserSet_andDeleteTokenKey() {
        // given
        String token = "rft_token_12345";
        String tokenKey = "refresh_token:" + token;

        doReturn(valueOps).when(redisTemplate).opsForValue(); // [web:54]
        doReturn(setOps).when(redisTemplate).opsForSet();

        when(valueOps.get(tokenKey)).thenReturn("5");

        // when
        store.delete(token);

        // then
        String userTokenKey = "user_tokens:5";

        verify(valueOps).get(tokenKey);
        verify(setOps).remove(userTokenKey, token);
        verify(redisTemplate).delete(tokenKey);

        verifyNoMoreInteractions(valueOps, setOps);
    }

    @Test
    @DisplayName("delete(): refresh_token:{token}에서 userId를 못 찾으면 Set 제거 없이 tokenKey만 삭제")
    void delete_shouldOnlyDeleteTokenKey_whenUserIdNotFound() {
        // given
        String token = "rft_token_12345";
        String tokenKey = "refresh_token:" + token;

        doReturn(valueOps).when(redisTemplate).opsForValue();
        when(valueOps.get(tokenKey)).thenReturn(null);

        // when
        store.delete(token);

        // then
        verify(valueOps).get(tokenKey);
        verify(redisTemplate).delete(tokenKey);
        verifyNoInteractions(setOps); // userId 없으니 opsForSet 자체가 불필요

        verifyNoMoreInteractions(valueOps);
    }

    @Test
    @DisplayName("deleteAllByUserId(): user_tokens:{userId}에 있는 모든 token의 refresh_token:{token} 키 삭제 후 userTokenKey 삭제")
    void deleteAllByUserId_shouldDeleteAllTokens_andDeleteUserTokenKey() {
        // given
        Long userId = 7L;
        String userTokenKey = "user_tokens:" + userId;

        doReturn(setOps).when(redisTemplate).opsForSet(); // members를 쓰므로 setOps만 필요
        when(setOps.members(userTokenKey)).thenReturn(Set.of("t1_1234567890", "t2_1234567890"));

        // when
        store.deleteAllByUserId(userId);

        // then
        verify(setOps).members(userTokenKey);
        verify(redisTemplate).delete("refresh_token:t1_1234567890");
        verify(redisTemplate).delete("refresh_token:t2_1234567890");
        verify(redisTemplate).delete(userTokenKey);

        verifyNoMoreInteractions(setOps);
        verifyNoInteractions(valueOps);
    }

    @Test
    @DisplayName("deleteAllByUserId(): tokens가 null/empty면 아무 것도 삭제하지 않음")
    void deleteAllByUserId_shouldDoNothing_whenNoTokens() {
        // given
        Long userId = 7L;
        String userTokenKey = "user_tokens:" + userId;

        doReturn(setOps).when(redisTemplate).opsForSet();
        when(setOps.members(userTokenKey)).thenReturn(Set.of());

        // when
        store.deleteAllByUserId(userId);

        // then
        verify(setOps).members(userTokenKey);
        verify(redisTemplate, never()).delete(startsWith("refresh_token:"));
        verify(redisTemplate, never()).delete(eq(userTokenKey));

        verifyNoMoreInteractions(setOps);
        verifyNoInteractions(valueOps);
    }

    @Test
    @DisplayName("exists(): refresh_token:{token} 키 존재 여부 반환")
    void exists_shouldReturnTrue_whenKeyExists() {
        // given
        String token = "rft_token_12345";
        String tokenKey = "refresh_token:" + token;

        when(redisTemplate.hasKey(tokenKey)).thenReturn(true);

        // when
        boolean exists = store.exists(token);

        // then
        assertThat(exists).isTrue();
        verify(redisTemplate).hasKey(tokenKey);

        verifyNoInteractions(valueOps, setOps);
    }
}
