package com.shoppingmall.ecommerceapi.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shoppingmall.ecommerceapi.domain.user.entity.enums.UserGrade;
import com.shoppingmall.ecommerceapi.domain.user.entity.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 간단 정보 응답 DTO
 * 목록 조회 시 사용 (민감 정보 제외)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 간단 정보 응답")
public class UserSimpleResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "이메일", example = "user@gmail.com")
    private String email;

    @Schema(description = "이름/닉네임", example = "홍길동")
    private String name;

    @Schema(description = "권한", example = "USER")
    private UserRole role;

    @Schema(description = "등급", example = "BASIC")
    private UserGrade grade;

    @JsonProperty("total_purchase_amount")
    @Schema(description = "누적 구매 금액", example = "150000")
    private Long totalPurchaseAmount;

    @JsonProperty("is_active")
    @Schema(description = "활성화 여부", example = "true")
    private Boolean isActive;

    @JsonProperty("created_at")
    @Schema(description = "가입 일시", example = "2026-01-01T12:00:00")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    @Schema(description = "수정 일시", example = "2026-01-01T12:00:00")
    private LocalDateTime updatedAt;
}
