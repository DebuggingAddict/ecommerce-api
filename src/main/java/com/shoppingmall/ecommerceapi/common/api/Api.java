package com.shoppingmall.ecommerceapi.common.api;

import com.shoppingmall.ecommerceapi.common.code.ApiCode;
import com.shoppingmall.ecommerceapi.common.code.CommonResultCode;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Api<T> {

  private Result result;

  @Valid
  private T body;

  public static <T> Api<T> OK(T data) {
    var api = new Api<T>();
    api.result = Result.of(CommonResultCode.OK);
    api.body = data;
    return api;
  }

  public static <T> Api<T> CREATED(T data) {
    var api = new Api<T>();
    api.result = Result.of(CommonResultCode.CREATED);
    api.body = data;
    return api;
  }

  public static <T> Api<T> ERROR(ApiCode code) {
    var api = new Api<T>();
    api.result = Result.of(code, "오류");
    api.body = null;
    return api;
  }

  public static <T> Api<T> ERROR(ApiCode code, String description) {
    var api = new Api<T>();
    api.result = Result.of(code, description);
    api.body = null;
    return api;
  }
}

