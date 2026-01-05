package com.shoppingmall.ecommerceapi.common.security.util;

import com.shoppingmall.ecommerceapi.domain.user.entity.enums.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;

    public JwtTokenProvider(
            @Value("${token.secret.key}") String secret,
            @Value("${token.access-token.plus-hour}") long accessTokenValidityInHours,
            @Value("${token.refresh-token.plus-hour}") long refreshTokenValidityInHours) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityInMilliseconds = accessTokenValidityInHours * 60 * 60 * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInHours * 60 * 60 * 1000;
    }

    /**
     * Access Token 생성 (userId + role 포함)
     */
    public String generateAccessToken(Long userId, UserRole role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Refresh Token 생성 (UUID 기반)
     */
    public String generateRefreshToken() {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

        return Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 토큰에서 userId 추출
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 토큰에서 role 추출
     */
    public UserRole getRoleFromToken(String token) {
        Claims claims = parseClaims(token);
        String role = claims.get("role", String.class);
        return UserRole.valueOf(role);
    }

    /**
     * 토큰 검증
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("만료된 JWT 토큰입니다: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.debug("지원하지 않는 JWT 토큰입니다: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.debug("잘못된 형식의 JWT 토큰입니다: {}", e.getMessage());
        } catch (JwtException e) {
            log.debug("JWT 토큰이 유효하지 않습니다: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.debug("JWT 토큰이 비어있습니다: {}", e.getMessage());
        }
        return false;
    }

    /**
     *  토큰의 남은 유효시간 반환 (밀리초)
     * - 블랙리스트에 추가할 때 TTL 설정용
     */
    public long getTokenRemainingTime(String token) {
        try {
            Claims claims = parseClaims(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();

            long remainingTime = expiration.getTime() - now.getTime();
            return Math.max(remainingTime, 0); // 음수면 0 반환

        } catch (ExpiredJwtException e) {
            log.debug("이미 만료된 토큰입니다: {}", e.getMessage());
            return 0;
        } catch (Exception e) {
            log.error("토큰 남은 시간 계산 실패", e);
            return 0;
        }
    }

    /**
     *  Access Token 유효시간 반환 (초 단위)
     * - 클라이언트에게 전달용
     */
    public long getAccessTokenValidityInSeconds() {
        return accessTokenValidityInMilliseconds / 1000;
    }

    /**
     *  Refresh Token 유효시간 반환 (초 단위)
     * - 클라이언트에게 전달용
     */
    public long getRefreshTokenValidityInSeconds() {
        return refreshTokenValidityInMilliseconds / 1000;
    }

    /**
     *  토큰 만료 시간 확인
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration();
        } catch (Exception e) {
            log.error("토큰 만료 시간 추출 실패", e);
            return null;
        }
    }

    /**
     *  토큰이 만료되었는지 확인
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration != null && expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 토큰 파싱
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
