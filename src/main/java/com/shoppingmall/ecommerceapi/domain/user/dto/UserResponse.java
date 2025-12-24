package com.shoppingmall.ecommerceapi.domain.user.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

  private Long id;
  private String email;
  private String username;
  private String name;
  private String phone;
  private String postalCode;
  private String address;
  private String addressDetail;
  private String role;
  private String grade;
  private Long totalPurchaseAmount;
  private Boolean isActive;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
