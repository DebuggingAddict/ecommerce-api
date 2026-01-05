package com.shoppingmall.ecommerceapi.domain.user.entity;

import com.shoppingmall.ecommerceapi.domain.user.entity.enums.UserGrade;
import com.shoppingmall.ecommerceapi.domain.user.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자 엔티티 (User 도메인)
 * - OAuth2 전용 (password 제거)
 * - 프로필/등급/역할 관리
 */
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_email", columnList = "email"),
                @Index(name = "idx_is_active", columnList = "is_active")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * 이메일 (nullable: 일부 소셜 로그인은 email 제공 안 할 수 있음)
   */
  @Column(unique = true, length = 100)
  private String email;

  /**
   * 사용자 이름/닉네임
   */
  @Column(nullable = false, length = 20)
  private String name;

  /**
   * 전화번호
   */
  @Column(length = 13)
  private String phone;

  /**
   * 우편번호
   */
  @Column(name = "zip_code", length = 5)
  private String zipCode;

  /**
   * 주소
   */
  @Column(length = 200)
  private String address;

  /**
   * 상세주소
   */
  @Column(name = "address_detail", length = 100)
  private String addressDetail;

  /**
   * 권한 (USER, ADMIN)
   */
  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private UserRole role = UserRole.USER;

  /**
   * 등급 (BASIC, VIP)
   */
  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private UserGrade grade = UserGrade.BASIC;

  /**
   * 누적 구매 금액 (등급 산정 기준)
   */
  @Builder.Default
  @Column(name = "total_purchase_amount", nullable = false)
  private Long totalPurchaseAmount = 0L;

  /**
   * 계정 활성화 여부
   */
  @Builder.Default
  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  /**
   * 생성 일시
   */
  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /**
   * 수정 일시
   */
  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /**
   * 탈퇴 일시
   */
  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  // =====================================================
  // 비즈니스 메서드
  // =====================================================

  /**
   * 회원 정보 수정
   *
   * @param name 이름
   * @param phone 전화번호
   * @param zipCode 우편번호
   * @param address 주소
   * @param addressDetail 상세주소
   */
  public void updateProfile(String name, String phone,
                            String zipCode, String address, String addressDetail) {
    if (name != null && !name.isBlank()) {
      this.name = name;
    }
    this.phone = phone;
    this.zipCode = zipCode;
    this.address = address;
    this.addressDetail = addressDetail;
  }

  /**
   * 구매 금액 추가 (주문 완료 시)
   *
   * @param amount 구매 금액
   */
  public void addPurchaseAmount(Long amount) {
    if (amount != null && amount > 0) {
      this.totalPurchaseAmount += amount;
    }
  }

  /**
   * 등급 변경 (관리자 전용 또는 자동 등급 산정)
   *
   * @param grade 새 등급
   */
  public void updateGrade(UserGrade grade) {
    if (grade != null) {
      this.grade = grade;
    }
  }

  /**
   * 역할 변경 (관리자 전용)
   *
   * @param role 새 역할
   */
  public void updateRole(UserRole role) {
    if (role != null) {
      this.role = role;
    }
  }

  /**
   * 계정 비활성화 (회원 탈퇴)
   */
  public void deactivate() {
    this.isActive = false;
    this.deletedAt = LocalDateTime.now();
  }

  /**
   * 계정 활성화 (관리자 복구)
   */
  public void activate() {
    this.isActive = true;
    this.deletedAt = null;
  }

  /**
   * 활성 계정 여부 확인
   *
   * @return 활성화 상태이면 true
   */
  public boolean isActivated() {
    return this.isActive && this.deletedAt == null;
  }
}
