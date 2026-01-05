package com.shoppingmall.ecommerceapi.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 프로필 수정 요청 DTO
 * OAuth2 전용 (username 제거)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 프로필 수정 요청")
@Builder
public class UserProfileUpdateRequest {

  @Schema(description = "이름/닉네임", example = "홍길동", required = true)
  @NotBlank(message = "이름은 필수입니다")
  @Size(min = 2, max = 20, message = "이름은 2~20자이어야 합니다")
  @Pattern(regexp = "^[가-힣a-zA-Z0-9\\s]+$", message = "이름은 한글, 영문, 숫자, 공백만 가능합니다")
  private String name;

  @Schema(description = "전화번호", example = "010-1234-5678")
  @Pattern(
          regexp = "^010-\\d{4}-\\d{4}$",
          message = "전화번호는 010-XXXX-XXXX 형식이어야 합니다"
  )
  private String phone;

  @Schema(description = "우편번호", example = "12345")
  @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자여야 합니다")
  private String zipCode;

  @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
  @Size(min = 5, max = 200, message = "주소는 5~200자이어야 합니다")
  private String address;

  @Schema(description = "상세주소", example = "101동 101호")
  @Size(min = 1, max = 100, message = "상세주소는 1~100자이어야 합니다")
  private String addressDetail;
}
