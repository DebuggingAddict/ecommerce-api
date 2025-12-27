package com.shoppingmall.ecommerceapi.domain.product.dto;

import com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductCategory;
import com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ProductUpdateRequest {

  @NotBlank(message = "상품 이름 입력은 필수입니다.")
  @Size(min = 2, max = 50, message = "이름은 2~50자 사이로 작성해주세요.")
  private String name;

  @Size(max = 200, message = "상품 설명은 최대 200자 까지 작성 가능합니다.")
  private String description;

  @NotNull(message = "상품 가격 입력은 필수입니다.")
  @Min(0)
  @Max(50000)
  private Integer price;

  @NotNull(message = "카테고리 입력은 필수입니다.")
  private ProductCategory category;

  @NotNull(message = "상태는 필수입니다.")
  private ProductStatus status;

  @NotNull(message = "재고 수량 입력은 필수입니다.")
  @Min(0)
  @Max(10000)
  private Integer stock;

  private String imgSrc;
}
