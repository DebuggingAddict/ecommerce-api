package com.shoppingmall.ecommerceapi.domain.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

  @NotBlank(message = "우편번호는 필수입니다")
  @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자여야 합니다")
  private String zipCode;

  @NotBlank(message = "주소는 필수입니다")
  @Size(min = 5, max = 200, message = "주소는 5자 이상 200자 이하여야 합니다")
  private String address;

  @NotBlank(message = "상세주소는 필수입니다")
  @Size(min = 1, max = 100, message = "상세주소는 1자 이상 100자 이하여야 합니다")
  private String detailAddress;

  @NotEmpty(message = "주문 아이템은 최소 1개 이상이어야 합니다")
  @Valid
  private List<CreateOrderItemRequest> orderItems;

  @NotNull(message = "총 주문 금액은 필수입니다")
  @DecimalMin(value = "0.01", message = "총 주문 금액은 0보다 커야 합니다")
  private BigDecimal totalPrice;
}
