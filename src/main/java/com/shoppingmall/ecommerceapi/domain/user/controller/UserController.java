package com.shoppingmall.ecommerceapi.domain.user.controller;

import com.shoppingmall.ecommerceapi.common.api.Api;
import com.shoppingmall.ecommerceapi.common.exception.BusinessException;
import com.shoppingmall.ecommerceapi.domain.user.exception.UserErrorCode;
import com.shoppingmall.ecommerceapi.domain.user.dto.UserProfileResponse;
import com.shoppingmall.ecommerceapi.domain.user.dto.UserProfileUpdateRequest;
import com.shoppingmall.ecommerceapi.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 사용자 API 컨트롤러 (User 도메인)
 * OAuth2 전용, BASIC/VIP 등급만 사용
 */
@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    // =====================================================
    // 프로필 조회
    // =====================================================

    /**
     * 내 프로필 조회
     */
    @Operation(
            summary = "내 프로필 조회",
            description = "인증된 사용자의 전체 프로필 정보를 조회합니다"
    )
    @GetMapping("/me")
    public Api<UserProfileResponse> getMyProfile() {
        Long userId = getCurrentUserId();
        UserProfileResponse response = userService.getMyProfile(userId);
        return Api.OK(response);
    }

    /**
     * 특정 사용자 공개 정보 조회
     */
    /*
    @Operation(
            summary = "사용자 공개 정보 조회",
            description = "특정 사용자의 공개 정보만 조회합니다 (이름, 등급)"
    )
    @GetMapping("/{userId}")
    public Api<UserProfileResponse> getUserPublicInfo(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long userId
    ) {
        UserProfileResponse response = userService.getUserPublicInfo(userId);
        return Api.OK(response);
    }
    */

    // =====================================================
    // 프로필 수정
    // =====================================================

    /**
     * 프로필 수정
     */
    @Operation(
            summary = "프로필 수정",
            description = "사용자 프로필 정보를 수정합니다 (이름, 전화번호, 주소)"
    )
    @PutMapping("/me")
    public Api<UserProfileResponse> updateProfile(
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        Long userId = getCurrentUserId();
        UserProfileResponse response = userService.updateProfile(userId, request);
        return Api.OK(response);
    }


    // =====================================================
    // 계정 관리
    // =====================================================

    /**
     * 회원 탈퇴
     */
    @Operation(
            summary = "회원 탈퇴",
            description = "계정을 비활성화합니다 (복구 가능)"
    )
    @DeleteMapping("/me")
    public Api<Map<String, String>> deactivateAccount() {
        Long userId = getCurrentUserId();
        userService.deactivateAccount(userId);

        Map<String, String> result = new HashMap<>();
        result.put("message", "회원 탈퇴가 완료되었습니다");

        return Api.OK(result);
    }


    // =====================================================
    // Helper Methods
    // =====================================================

    /**
     * 현재 인증된 사용자 ID 추출
     *
     * @return 사용자 ID
     * @throws BusinessException 인증되지 않은 경우
     */
    private Long getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }

        throw new BusinessException(UserErrorCode.UNAUTHORIZED_USER);
    }
}
