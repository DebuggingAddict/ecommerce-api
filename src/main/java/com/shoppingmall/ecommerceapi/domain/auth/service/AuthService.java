package com.shoppingmall.ecommerceapi.domain.auth.service;

import com.shoppingmall.ecommerceapi.common.security.util.JwtTokenProvider;
import com.shoppingmall.ecommerceapi.domain.auth.converter.OAuthConverter;
import com.shoppingmall.ecommerceapi.domain.auth.dto.TokenResponse;
import com.shoppingmall.ecommerceapi.domain.auth.entity.SocialAccount;
import com.shoppingmall.ecommerceapi.domain.auth.exception.AuthErrorCode;
import com.shoppingmall.ecommerceapi.domain.auth.repository.RefreshTokenStore;
import com.shoppingmall.ecommerceapi.domain.auth.repository.SocialAccountRepository;
import com.shoppingmall.ecommerceapi.domain.cart.service.CartService;
import com.shoppingmall.ecommerceapi.domain.user.entity.User;
import com.shoppingmall.ecommerceapi.domain.user.entity.enums.UserRole;
import com.shoppingmall.ecommerceapi.domain.user.repository.UserRepository;
import com.shoppingmall.ecommerceapi.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final RefreshTokenStore refreshTokenStore;
    private final JwtTokenProvider jwtTokenProvider;
    private final OAuthConverter oAuthConverter;
    private final CartService cartService;
    private final TokenBlacklistService tokenBlacklistService;

    // Refresh Token 만료 시간 (yml에서 주입)
    @Value("${token.refresh-token.plus-hour}")
    private long refreshTokenValidityInHours;

    /**
     * OAuth2 로그인 성공 시 사용자 정보 처리
     */
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        log.info("OAuth2 로그인 시도: provider={}", provider);

        // OAuth2 제공자별 사용자 정보 추출
        OAuthConverter.OAuthUserInfo oAuthUserInfo = oAuthConverter.extractUserInfo(provider, attributes);

        // 사용자 조회 또는 생성
        User user = getOrCreateUser(oAuthUserInfo);

        log.info("OAuth2 사용자 처리 완료: userId={}, email={}", user.getId(), user.getEmail());

        // Spring Security용 OAuth2User 반환
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                attributes,
                "sub" // Google의 사용자 식별자 필드명
        ) {
            @Override
            public String getName() {
                return user.getId().toString(); // userId 반환
            }
        };
    }

    /**
     * 사용자 조회 또는 생성
     */
    private User getOrCreateUser(OAuthConverter.OAuthUserInfo oAuthUserInfo) {
        return userRepository.findByEmail(oAuthUserInfo.getEmail())
                .orElseGet(() -> createNewUser(oAuthUserInfo));
    }

    /**
     * 신규 사용자 생성
     */
    private User createNewUser(OAuthConverter.OAuthUserInfo oAuthUserInfo) {
        // User 생성
        User user = oAuthConverter.toNewUser(oAuthUserInfo);
        user = userRepository.save(user);

        // SocialAccount 생성
        SocialAccount socialAccount = SocialAccount.builder()
                .provider(oAuthUserInfo.getProvider())
                .providerUserId(oAuthUserInfo.getProviderId())
                .userId(user.getId())
                .email(oAuthUserInfo.getEmail())
                .build();
        socialAccountRepository.save(socialAccount);

        cartService.createCartForUser(user.getId());

        log.info("신규 사용자 생성: userId={}, provider={}", user.getId(), oAuthUserInfo.getProvider());
        return user;
    }


    /**
     * JWT 토큰 발급 (Access + Refresh)
     */
    @Transactional
    public TokenResponse generateTokens(Long userId, UserRole role, String deviceInfo) {
        String accessToken = jwtTokenProvider.generateAccessToken(userId, role);
        String refreshToken = jwtTokenProvider.generateRefreshToken();

        // Redis에 Refresh Token 저장
        Duration expiration = Duration.ofHours(refreshTokenValidityInHours);
        refreshTokenStore.save(userId, refreshToken, deviceInfo, expiration);

        log.info("토큰 발급 완료: userId={}, role={}, device={}", userId, role, deviceInfo);

        return TokenResponse.of(accessToken, refreshToken);
    }

    /**
     *  Access Token 재발급 (기존 Access Token 만료 처리 포함)
     */
    @Transactional
    public TokenResponse refreshAccessToken(String refreshToken, String deviceInfo, String oldAccessToken) {
        // 1. Refresh Token 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 2. Redis에서 userId 조회
        Long userId = refreshTokenStore.findUserIdByToken(refreshToken)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND));

        // 3. User 조회하여 role 가져오기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_FOUND));

        // 4.  기존 Access Token을 블랙리스트에 추가
        if (oldAccessToken != null && !oldAccessToken.isEmpty()) {
            long remainingTime = jwtTokenProvider.getTokenRemainingTime(oldAccessToken);

            if (remainingTime > 0) {
                tokenBlacklistService.addToBlacklist(oldAccessToken, remainingTime);
                log.info("기존 Access Token 블랙리스트 추가: userId={}, remainingTime={}ms", userId, remainingTime);
            } else {
                log.debug("기존 Access Token 이미 만료됨, 블랙리스트 추가 불필요: userId={}", userId);
            }
        }

        // 5. 새로운 토큰 발급 (Refresh Token Rotation)
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, user.getRole());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken();

        // 토큰 비교
        if (oldAccessToken != null && oldAccessToken.equals(newAccessToken)) {
            log.error("❌❌❌ 경고: 기존 토큰과 새 토큰이 동일합니다! ❌❌❌");
        }

        // 6. 기존 Refresh Token 삭제 후 새로운 토큰 저장
        refreshTokenStore.delete(refreshToken);
        Duration expiration = Duration.ofHours(refreshTokenValidityInHours);
        refreshTokenStore.save(userId, newRefreshToken, deviceInfo, expiration);

        log.info("토큰 갱신 완료: userId={}, role={}, device={}", userId, user.getRole(), deviceInfo);

        return TokenResponse.of(newAccessToken, newRefreshToken);
    }

    /**
     *  로그아웃 (Access Token 블랙리스트 추가)
     */
    @Transactional
    public void logout(Long userId, String refreshToken, String accessToken) {
        // 1. Refresh Token 삭제
        refreshTokenStore.delete(refreshToken);

        // 2.  Access Token 블랙리스트 추가
        if (accessToken != null && !accessToken.isEmpty()) {
            long remainingTime = jwtTokenProvider.getTokenRemainingTime(accessToken);

            if (remainingTime > 0) {
                tokenBlacklistService.addToBlacklist(accessToken, remainingTime);
                log.info("로그아웃 시 Access Token 블랙리스트 추가: userId={}", userId);
            }
        }

        log.info("로그아웃 완료: userId={}", userId);
    }

    /**
     *  전체 디바이스 로그아웃
     * 참고: 모든 디바이스의 Access Token을 블랙리스트에 추가하려면
     * Redis에 userId별 Access Token 목록을 별도로 관리해야 합니다.
     * 현재는 Refresh Token만 삭제하여 재발급을 막는 방식입니다.
     */
    @Transactional
    public void logoutAllDevices(Long userId) {
        // 모든 Refresh Token 삭제
        refreshTokenStore.deleteAllByUserId(userId);

        log.info("전체 디바이스 로그아웃 완료: userId={}", userId);
        log.info("참고: 기존 Access Token은 만료 시간까지 유효합니다. 완전한 무효화를 원한다면 Access Token 관리 전략이 필요합니다.");
    }
}
