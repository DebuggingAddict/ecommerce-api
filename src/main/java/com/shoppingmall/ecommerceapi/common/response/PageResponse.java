package com.shoppingmall.ecommerceapi.common.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

  private List<T> content;
  private PageInfo pageInfo;

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PageInfo {

    private Integer currentPage;
    private Integer pageSize;
    private Long totalElements;
    private Integer totalPages;
    private String currentSort;
    private Boolean isFirst;
    private Boolean isLast;
    private Boolean hasNext;
    private Boolean hasPrevious;
  }

  /**
   * Spring Data JPA의 Page 객체를 PageResponse로 변환
   *
   * @param page     Spring Data JPA Page 객체
   * @param content  변환된 DTO 리스트
   * @param sortType 현재 정렬 방식 (예: "PRICE_DESC")
   * @return PageResponse 객체
   */
  public static <T> PageResponse<T> of(Page<?> page, List<T> content, String sortType) {
    PageInfo pageInfo = PageInfo.builder()
        .currentPage(page.getNumber())
        .pageSize(page.getSize())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .currentSort(sortType)
        .isFirst(page.isFirst())
        .isLast(page.isLast())
        .hasNext(page.hasNext())
        .hasPrevious(page.hasPrevious())
        .build();

    return PageResponse.<T>builder()
        .content(content)
        .pageInfo(pageInfo)
        .build();
  }

  /**
   * sortType 없이 기본 정렬로 변환
   */
  public static <T> PageResponse<T> of(Page<?> page, List<T> content) {
    return of(page, content, null);
  }
}
