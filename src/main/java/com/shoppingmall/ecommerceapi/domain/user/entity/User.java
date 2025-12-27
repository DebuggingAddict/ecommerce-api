package com.shoppingmall.ecommerceapi.domain.user.entity;

import com.shoppingmall.ecommerceapi.domain.user.entity.enums.UserGrade;
import com.shoppingmall.ecommerceapi.domain.user.entity.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 100)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false, length = 20)
  private String username;

  @Column(nullable = false, length = 20)
  private String name;

  @Column(nullable = false, length = 13)
  private String phone;

  @Column(name = "zip_code", length = 5)
  private String zipCode;

  @Column(length = 200)
  private String address;

  @Column(length = 100)
  private String addressDetail;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private UserRole role = UserRole.USER;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private UserGrade grade = UserGrade.BASIC;

  @Builder.Default
  @Column(nullable = false)
  private Long totalPurchaseAmount = 0L;

  @Builder.Default
  @Column(nullable = false)
  private Boolean isActive = true;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @Column
  private LocalDateTime deletedAt;

  // 비즈니스 메서드

  /**
   * 회원 정보 수정
   */
  public void updateProfile(String username, String name, String phone,
      String zipCode, String address, String addressDetail) {
    this.username = username;
    this.name = name;
    this.phone = phone;
    this.zipCode = zipCode;
    this.address = address;
    this.addressDetail = addressDetail;
  }

  /**
   * 비밀번호 변경
   */
  public void updatePassword(String encodedPassword) {
    this.password = encodedPassword;
  }

  /**
   * 구매 금액 추가
   */
  public void addPurchaseAmount(Long amount) {
    this.totalPurchaseAmount += amount;
  }

  /**
   * 등급 변경 (관리자 전용)
   */
  public void updateGrade(UserGrade grade) {
    this.grade = grade;
  }

  /**
   * 역할 변경 (관리자 전용)
   */
  public void updateRole(UserRole role) {
    this.role = role;
  }

  /**
   * 계정 비활성화 (회원 탈퇴)
   */
  public void deactivate() {
    this.isActive = false;
    this.deletedAt = LocalDateTime.now();
  }

  /**
   * 계정 활성화 (관리자 전용)
   */
  public void activate() {
    this.isActive = true;
    this.deletedAt = null;
  }
}
