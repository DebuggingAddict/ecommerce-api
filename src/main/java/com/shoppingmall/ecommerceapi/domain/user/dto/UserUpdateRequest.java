// UserUpdateRequest.java
package com.shoppingmall.ecommerceapi.domain.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

  @Size(min = 2, max = 20, message = "유저네임은 2~20자이어야 합니다")
  @Pattern(regexp = "^[가-힣a-zA-Z]+$", message = "유저네임은 한글 또는 영문만 가능합니다")
  private String username;

  @Size(min = 2, max = 20, message = "이름은 2~20자이어야 합니다")
  @Pattern(regexp = "^[가-힣a-zA-Z]+$", message = "이름은 한글 또는 영문만 가능합니다")
  private String name;

  @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-XXXX-XXXX 형식이어야 합니다")
  private String phone;

  @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자여야 합니다")
  private String zipCode;

  @Size(min = 5, max = 200, message = "기본주소는 5~200자이어야 합니다")
  private String address;

  @Size(min = 1, max = 100, message = "상세주소는 1~100자이어야 합니다")
  private String addressDetail;
}
