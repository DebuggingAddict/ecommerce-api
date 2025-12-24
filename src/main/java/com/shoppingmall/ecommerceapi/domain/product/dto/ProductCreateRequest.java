package com.shoppingmall.ecommerceapi.domain.product.dto;

import com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductCategory;
import com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductCreateRequest {

  @NotBlank(message = "상품 이름 입력은 필수입니다.")
  @Size(min = 2, max = 50, message = "이름은 2~50자 사이로 작성해주세요")
  private String name;

  @Size(max = 200, message = "상품 설명은 최대 200자 까지 작성 가능합니다.")
  private String description;

  @NotNull(message = "가격 입력은 필수입니다.")
  @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
  @Max(value = 50000, message = "가격은 최대 50000원까지 입력 가능합니다.")
  private Integer price;

  @NotNull(message = "카테고리 입력은 필수입니다.")
  private ProductCategory category;

  private ProductStatus status = ProductStatus.SOLD_OUT;

  @NotNull(message = "재고 수량 입력은 필수입니다.")
  @Min(value = 0, message = "재고는 0개 이상이어야 합니다.")
  @Max(value = 10000, message = "재고는 최대 10000개 까지 등록 가능합니다.")
  private Integer stock = 0;

  private String imgSrc;


}
