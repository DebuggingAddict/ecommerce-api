package com.shoppingmall.ecommerceapi.common.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
public class PageRequestDTO {

  private String sortType;  // 또는 status (필터링용)
  private Integer page = 0;  // 기본값: 0
  private Integer size = 10;  // 기본값: 10
  private String sort;  // 예: "created_at,desc"

  /**
   * Pageable 객체로 변환
   */
  public Pageable toPageable() {
    if (sort != null && !sort.isEmpty()) {
      String[] sortParams = sort.split(",");
      Sort.Direction direction = sortParams.length > 1 &&
          sortParams[1].equalsIgnoreCase("desc")
          ? Sort.Direction.DESC
          : Sort.Direction.ASC;
      return PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
    }
    return PageRequest.of(page, size);
  }
}
