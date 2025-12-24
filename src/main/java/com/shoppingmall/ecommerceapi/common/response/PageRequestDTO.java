package com.shoppingmall.ecommerceapi.common.response;

public class PageRequestDTO {

  private String sortType;  // 또는 status (필터링용)
  private Integer page = 0;  // 기본값: 0
  private Integer size = 10;  // 기본값: 10
  private String sort;  // 예: "created_at,desc"
}
