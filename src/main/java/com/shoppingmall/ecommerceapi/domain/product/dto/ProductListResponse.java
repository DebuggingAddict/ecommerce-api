package com.shoppingmall.ecommerceapi.domain.product.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ProductListResponse {

  private List<ProductResponse> products;
  private PageInfo pageInfo;

  @Getter
  @Builder
  public static class PageInfo {

    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private String currentSort;
    private boolean isFirst;
    private boolean isLast;
    private boolean hasNext;
    private boolean hasPrevious;
  }
}
