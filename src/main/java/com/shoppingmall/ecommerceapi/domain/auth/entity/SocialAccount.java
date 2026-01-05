package com.shoppingmall.ecommerceapi.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 소셜 계정 연결 엔티티 (Auth 도메인)
 * - OAuth2 Provider와 내부 User를 연결
 * - (provider, providerUserId) 조합으로 고유성 보장
 */
@Entity
@Table(
        name = "social_accounts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_provider_user",
                columnNames = {"provider", "provider_user_id"}
        ),
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id"),
                @Index(name = "idx_provider_user", columnList = "provider,provider_user_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * OAuth2 Provider (google 등)
     */
    @Column(nullable = false, length = 20)
    private String provider;

    /**
     * Provider의 사용자 고유 식별자
     * - Google: OIDC sub
     * - Kakao: id
     * - Naver: id
     */
    @Column(name = "provider_user_id", nullable = false, length = 100)
    private String providerUserId;

    /**
     * 연결된 내부 사용자 ID (User 도메인 참조)
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Provider에서 제공한 이메일 (선택적)
     */
    @Column(length = 100)
    private String email;

    /**
     * 소셜 계정 연결 일시
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // =====================================================
    // 생성자 (Builder 패턴)
    // =====================================================

    @Builder
    public SocialAccount(String provider, String providerUserId, Long userId, String email) {
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.userId = userId;
        this.email = email;
    }

    // =====================================================
    // 생성 메서드 (Factory Method)
    // =====================================================

    /**
     * 소셜 계정 연결 정보 생성
     */
    public static SocialAccount of(String provider, String providerUserId,
                                   Long userId, String email) {
        SocialAccount account = new SocialAccount();
        account.provider = provider;
        account.providerUserId = providerUserId;
        account.userId = userId;
        account.email = email;
        return account;
    }

    /**
     * 이메일 업데이트
     */
    public void updateEmail(String email) {
        this.email = email;
    }

    /**
     * Provider와 고유 ID가 일치하는지 확인
     */
    public boolean matches(String provider, String providerUserId) {
        return this.provider.equals(provider)
                && this.providerUserId.equals(providerUserId);
    }
}
