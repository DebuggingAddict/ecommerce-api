package com.shoppingmall.ecommerceapi.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ParameterObject
@Schema(name = "PageRequestDTO", description = "페이지 요청 정보 DTO")
public class PageRequestDTO {

  @Schema(description = "상태 필터링 예: STOP_SALE", example = "STOP_SALE")
  private String sortType;
  @Schema(description = "페이지 번호 (0부터 시작)", example = "0", defaultValue = "0")
  private Integer page = 0;
  @Schema(description = "페이지 크기", example = "5", defaultValue = "10")
  private Integer size = 10;
  @Schema(description = "정렬 : '필드명,asc' 또는 '필드명,desc'", example = "createdAt,desc")
  private String sort;

  /**
   * Pageable 객체로 변환 JPA는 엔티티 필드명을 사용하므로 변환 없이 그대로 전달
   */
  public Pageable toPageable() {
    if (sort != null && !sort.isEmpty()) {
      String[] sortParams = sort.split(",");
      String fieldName = sortParams[0].trim();  // 엔티티 필드명 그대로 사용
      Sort.Direction direction = sortParams.length > 1 &&
          sortParams[1].trim().equalsIgnoreCase("desc")
          ? Sort.Direction.DESC
          : Sort.Direction.ASC;
      return PageRequest.of(page, size, Sort.by(direction, fieldName));
    }
    return PageRequest.of(page, size);
  }
}
