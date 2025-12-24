package com.shoppingmall.ecommerceapi.domain.user.converter;

import com.shoppingmall.ecommerceapi.domain.user.dto.UserResponse;
import com.shoppingmall.ecommerceapi.domain.user.entity.User;
import org.springframework.stereotype.Component;

/**
 * User Entity와 DTO 간의 변환을 담당하는 컨버터
 */
@Component
public class UserConverter {

  /**
   * User Entity -> UserResponse DTO 변환
   *
   * @param user User 엔티티
   * @return UserResponse DTO
   */
  public UserResponse toResponse(User user) {
    if (user == null) {
      return null;
    }

    return UserResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .username(user.getUsername())
        .name(user.getName())
        .phone(user.getPhone())
        .postalCode(user.getPostalCode())
        .address(user.getAddress())
        .addressDetail(user.getAddressDetail())
        .role(user.getRole().name())
        .grade(user.getGrade().name())
        .totalPurchaseAmount(user.getTotalPurchaseAmount())
        .isActive(user.getIsActive())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .build();
  }

  /**
   * User Entity -> UserResponse DTO 변환 (간단 버전) 민감한 정보 제외
   *
   * @param user User 엔티티
   * @return UserResponse DTO (간단 버전)
   */
  public UserResponse toSimpleResponse(User user) {
    if (user == null) {
      return null;
    }

    return UserResponse.builder()
        .id(user.getId())
        .username(user.getUsername())
        .grade(user.getGrade().name())
        .build();
  }
}
