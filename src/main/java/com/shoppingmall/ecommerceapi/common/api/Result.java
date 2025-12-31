package com.shoppingmall.ecommerceapi.common.api;

import com.shoppingmall.ecommerceapi.common.code.ApiCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Result {

  private Integer code;         // 기존 resultCode
  private String message;       // 기존 resultMessage
  private String description;   // 기존 resultDescription (상세설명)

  public static Result of(ApiCode apiCode) {
    return Result.builder()
        .code(apiCode.getCode())
        .message(apiCode.toString())
        .description(null)
        .build();
  }

  public static Result of(ApiCode apiCode, String description) {
    return Result.builder()
        .code(apiCode.getCode())
        .message(apiCode.toString())
        .description(description)
        .build();
  }
}
