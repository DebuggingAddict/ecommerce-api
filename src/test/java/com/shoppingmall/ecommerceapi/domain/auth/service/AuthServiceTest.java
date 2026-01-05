package com.shoppingmall.ecommerceapi.domain.auth.service;

import com.shoppingmall.ecommerceapi.common.exception.BusinessException;
import com.shoppingmall.ecommerceapi.common.security.util.JwtTokenProvider;
import com.shoppingmall.ecommerceapi.domain.auth.converter.OAuthConverter;
import com.shoppingmall.ecommerceapi.domain.auth.dto.TokenResponse;
import com.shoppingmall.ecommerceapi.domain.auth.exception.AuthErrorCode;
import com.shoppingmall.ecommerceapi.domain.auth.repository.RefreshTokenStore;
import com.shoppingmall.ecommerceapi.domain.auth.repository.SocialAccountRepository;
import com.shoppingmall.ecommerceapi.domain.cart.service.CartService;
import com.shoppingmall.ecommerceapi.domain.user.entity.User;
import com.shoppingmall.ecommerceapi.domain.user.entity.enums.UserRole;
import com.shoppingmall.ecommerceapi.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock SocialAccountRepository socialAccountRepository;
    @Mock RefreshTokenStore refreshTokenStore;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock OAuthConverter oAuthConverter;
    @Mock CartService cartService;
    @Mock TokenBlacklistService tokenBlacklistService;

    @InjectMocks AuthService authService;

    @BeforeEach
    void setUp() {
        // @Value 필드 주입
        ReflectionTestUtils.setField(authService, "refreshTokenValidityInHours", 24L);
    }

    @Test
    @DisplayName("generateTokens(): access/refresh 발급 후 refreshTokenStore에 저장된다")
    void generateTokens_shouldSaveRefreshToken() {
        // given
        Long userId = 1L;
        UserRole role = UserRole.USER;
        String deviceInfo = "ios";
        when(jwtTokenProvider.generateAccessToken(userId, role)).thenReturn("access");
        when(jwtTokenProvider.generateRefreshToken()).thenReturn("refresh");

        // when
        TokenResponse res = authService.generateTokens(userId, role, deviceInfo);

        // then
        assertThat(res.getAccessToken()).isEqualTo("access");
        assertThat(res.getRefreshToken()).isEqualTo("refresh");

        ArgumentCaptor<Duration> expCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(refreshTokenStore).save(eq(userId), eq("refresh"), eq(deviceInfo), expCaptor.capture());
        assertThat(expCaptor.getValue()).isEqualTo(Duration.ofHours(24));
    }

    @Test
    @DisplayName("refreshAccessToken(): refreshToken이 유효하지 않으면 INVALID_REFRESH_TOKEN 예외")
    void refreshAccessToken_invalidRefreshToken_shouldThrow() {
        // given
        when(jwtTokenProvider.validateToken("bad-refresh")).thenReturn(false);

        // when / then
        assertThatThrownBy(() ->
                authService.refreshAccessToken("bad-refresh", "android", "old-access")
        ).isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);



        verifyNoInteractions(refreshTokenStore);
        verifyNoInteractions(tokenBlacklistService);
    }

    @Test
    @DisplayName("refreshAccessToken(): 저장된 refreshToken이 없으면 REFRESH_TOKEN_NOT_FOUND 예외")
    void refreshAccessToken_notFound_shouldThrow() {
        // given
        when(jwtTokenProvider.validateToken("refresh")).thenReturn(true);
        when(refreshTokenStore.findUserIdByToken("refresh")).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() ->
                authService.refreshAccessToken("refresh", "android", "old-access")
        ).isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND);

        verify(refreshTokenStore, never()).delete(anyString());
        verifyNoInteractions(tokenBlacklistService);
    }

    @Test
    @DisplayName("refreshAccessToken(): oldAccessToken 남은 시간이 >0 이면 블랙리스트에 추가 후 토큰 재발급/저장된다")
    void refreshAccessToken_shouldBlacklistOldToken_andRotateRefreshToken() {
        // given
        Long userId = 7L;
        User user = mock(User.class);
        when(user.getRole()).thenReturn(UserRole.USER);

        when(jwtTokenProvider.validateToken("refresh")).thenReturn(true);
        when(refreshTokenStore.findUserIdByToken("refresh")).thenReturn(Optional.of(userId));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(jwtTokenProvider.getTokenRemainingTime("old-access")).thenReturn(10_000L);
        when(jwtTokenProvider.generateAccessToken(userId, UserRole.USER)).thenReturn("new-access");
        when(jwtTokenProvider.generateRefreshToken()).thenReturn("new-refresh");

        // when
        TokenResponse res = authService.refreshAccessToken("refresh", "android", "old-access");

        // then
        assertThat(res.getAccessToken()).isEqualTo("new-access");
        assertThat(res.getRefreshToken()).isEqualTo("new-refresh");

        InOrder inOrder = inOrder(tokenBlacklistService, refreshTokenStore);
        inOrder.verify(tokenBlacklistService).addToBlacklist("old-access", 10_000L);
        inOrder.verify(refreshTokenStore).delete("refresh");
        inOrder.verify(refreshTokenStore).save(eq(userId), eq("new-refresh"), eq("android"), eq(Duration.ofHours(24)));
    }

    @Test
    @DisplayName("refreshAccessToken(): oldAccessToken이 null/empty면 블랙리스트 추가하지 않는다")
    void refreshAccessToken_noOldToken_shouldNotBlacklist() {
        // given
        Long userId = 7L;
        User user = mock(User.class);
        when(user.getRole()).thenReturn(UserRole.USER);

        when(jwtTokenProvider.validateToken("refresh")).thenReturn(true);
        when(refreshTokenStore.findUserIdByToken("refresh")).thenReturn(Optional.of(userId));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(jwtTokenProvider.generateAccessToken(userId, UserRole.USER)).thenReturn("new-access");
        when(jwtTokenProvider.generateRefreshToken()).thenReturn("new-refresh");

        // when
        authService.refreshAccessToken("refresh", "android", "");

        // then
        verify(tokenBlacklistService, never()).addToBlacklist(anyString(), anyLong());
        verify(refreshTokenStore).delete("refresh");
        verify(refreshTokenStore).save(eq(userId), eq("new-refresh"), eq("android"), eq(Duration.ofHours(24)));
    }

    @Test
    @DisplayName("logout(): refresh 삭제 + access 남은 시간이 >0 이면 블랙리스트 추가")
    void logout_shouldDeleteRefresh_andBlacklistAccess() {
        // given
        when(jwtTokenProvider.getTokenRemainingTime("access")).thenReturn(5000L);

        // when
        authService.logout(1L, "refresh", "access");

        // then
        verify(refreshTokenStore).delete("refresh");
        verify(tokenBlacklistService).addToBlacklist("access", 5000L);
    }

    @Test
    @DisplayName("logoutAllDevices(): userId 기준으로 refreshToken 전부 삭제")
    void logoutAllDevices_shouldDeleteAllRefreshTokens() {
        // when
        authService.logoutAllDevices(99L);

        // then
        verify(refreshTokenStore).deleteAllByUserId(99L);
        verifyNoInteractions(tokenBlacklistService);
    }
}
