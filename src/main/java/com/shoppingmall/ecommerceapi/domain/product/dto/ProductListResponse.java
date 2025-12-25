package com.shoppingmall.ecommerceapi.domain.product.dto;

import com.shoppingmall.ecommerceapi.common.response.PageResponse.PageInfo;
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

}
