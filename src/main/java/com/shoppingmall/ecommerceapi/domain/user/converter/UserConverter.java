package com.shoppingmall.ecommerceapi.domain.user.converter;

import com.shoppingmall.ecommerceapi.domain.user.dto.UserProfileResponse;
import com.shoppingmall.ecommerceapi.domain.user.dto.UserSimpleResponse;
import com.shoppingmall.ecommerceapi.domain.user.entity.User;
import org.springframework.stereotype.Component;

/**
 * User Entity와 DTO 간의 변환을 담당하는 컨버터
 * OAuth2 전용 (username 제거)
 */
@Component
public class UserConverter {

  /**
   * User Entity -> UserProfileResponse DTO 변환 (전체 정보)
   *
   * @param user User 엔티티
   * @return UserProfileResponse DTO
   */
  public UserProfileResponse toProfileResponse(User user) {
    if (user == null) {
      return null;
    }

    return UserProfileResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .phone(user.getPhone())
            .zipCode(user.getZipCode())
            .address(user.getAddress())
            .addressDetail(user.getAddressDetail())
            .role(user.getRole())
            .grade(user.getGrade())
            .totalPurchaseAmount(user.getTotalPurchaseAmount())
            .isActive(user.getIsActive())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
  }

  /**
   * User Entity -> UserSimpleResponse DTO 변환 (간단 버전)
   * 민감한 정보 제외 (주소, 전화번호 등)
   *
   * @param user User 엔티티
   * @return UserSimpleResponse DTO (간단 버전)
   */
  public UserSimpleResponse toSimpleResponse(User user) {
    if (user == null) {
      return null;
    }

    return UserSimpleResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .role(user.getRole())
            .grade(user.getGrade())
            .totalPurchaseAmount(user.getTotalPurchaseAmount())
            .isActive(user.getIsActive())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
  }

  /**
   * User Entity -> UserProfileResponse DTO 변환 (공개 정보만)
   * 댓글/리뷰 등에서 사용자 표시 시
   *
   * @param user User 엔티티
   * @return UserProfileResponse DTO (공개 정보만)
   */
  public UserProfileResponse toPublicResponse(User user) {
    if (user == null) {
      return null;
    }

    return UserProfileResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .grade(user.getGrade())
            .build();
  }
}
