package com.shoppingmall.ecommerceapi.domain.user.controller;

import com.shoppingmall.ecommerceapi.common.api.Api;
import com.shoppingmall.ecommerceapi.domain.user.dto.UserSimpleResponse;
import com.shoppingmall.ecommerceapi.domain.user.entity.enums.UserGrade;
import com.shoppingmall.ecommerceapi.domain.user.entity.enums.UserRole;
import com.shoppingmall.ecommerceapi.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Admin User", description = "관리자 사용자 관리 API")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;


    // =====================================================
    // 계정 관리
    // =====================================================

    /**
     * 계정 복구 (관리자 전용)
     */
    @Operation(
            summary = "계정 복구 (관리자)",
            description = "비활성화된 계정을 활성화합니다"
    )
    @PostMapping("/{userId}/activate")
    public Api<Map<String, String>> activateAccount(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long userId
    ) {
        userService.activateAccount(userId);

        Map<String, String> result = new HashMap<>();
        result.put("message", "계정이 복구되었습니다");

        return Api.OK(result);
    }

    // =====================================================
    // 등급/역할 관리 (관리자 전용)
    // =====================================================

    /**
     * 사용자 등급 변경 (관리자 전용)
     */
    @Operation(
            summary = "사용자 등급 변경 (관리자)",
            description = "사용자 등급을 BASIC 또는 VIP로 변경합니다"
    )
    @PutMapping("/{userId}/grade")
    public Api<Map<String, String>> updateGrade(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long userId,
            @Parameter(description = "새 등급", example = "VIP")
            @RequestParam UserGrade grade
    ) {
        userService.updateGrade(userId, grade);

        Map<String, String> result = new HashMap<>();
        result.put("message", "등급이 변경되었습니다");
        result.put("grade", grade.name());

        return Api.OK(result);
    }

    /**
     * 사용자 역할 변경 (관리자 전용)
     */
    @Operation(
            summary = "사용자 역할 변경 (관리자)",
            description = "사용자 역할을 변경합니다 (USER, ADMIN)"
    )
    @PutMapping("/{userId}/role")
    public Api<Map<String, String>> updateRole(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long userId,
            @Parameter(description = "새 역할", example = "ADMIN")
            @RequestParam UserRole role
    ) {
        userService.updateRole(userId, role);

        Map<String, String> result = new HashMap<>();
        result.put("message", "역할이 변경되었습니다");
        result.put("role", role.name());

        return Api.OK(result);
    }


    // =====================================================
    // 검색/필터링 (관리자 전용)
    // =====================================================

    /**
     * 이름으로 사용자 검색 (관리자 전용)
     */
    @Operation(
            summary = "이름으로 사용자 검색 (관리자)",
            description = "이름으로 사용자를 검색합니다 (부분 일치)"
    )
    @GetMapping("/search/name")
    public Api<List<UserSimpleResponse>> searchByName(
            @Parameter(description = "검색할 이름", example = "홍길동")
            @RequestParam String name
    ) {
        List<UserSimpleResponse> users = userService.searchByName(name);
        return Api.OK(users);
    }

    /**
     * 이메일로 사용자 검색 (관리자 전용)
     */
    @Operation(
            summary = "이메일로 사용자 검색 (관리자)",
            description = "이메일로 사용자를 검색합니다 (부분 일치)"
    )
    @GetMapping("/search/email")
    public Api<List<UserSimpleResponse>> searchByEmail(
            @Parameter(description = "검색할 이메일", example = "user@gmail.com")
            @RequestParam String email
    ) {
        List<UserSimpleResponse> users = userService.searchByEmail(email);
        return Api.OK(users);
    }

    /**
     * 등급별 사용자 목록 조회 (관리자 전용)
     */
    @Operation(
            summary = "등급별 사용자 목록 (관리자)",
            description = "특정 등급의 사용자 목록을 조회합니다"
    )
    @GetMapping("/grade/{grade}")
    public Api<List<UserSimpleResponse>> getUsersByGrade(
            @Parameter(description = "사용자 등급", example = "VIP")
            @PathVariable UserGrade grade
    ) {
        List<UserSimpleResponse> users = userService.getUsersByGrade(grade);
        return Api.OK(users);
    }

    /**
     * VIP 사용자 목록 조회 (관리자 전용)
     */
    @Operation(
            summary = "VIP 사용자 목록 (관리자)",
            description = "VIP 등급의 사용자 목록을 조회합니다"
    )
    @GetMapping("/vip")
    public Api<List<UserSimpleResponse>> getVipUsers() {
        List<UserSimpleResponse> users = userService.getVipUsers();
        return Api.OK(users);
    }

    /**
     * BASIC 사용자 목록 조회 (관리자 전용)
     */
    @Operation(
            summary = "BASIC 사용자 목록 (관리자)",
            description = "BASIC 등급의 사용자 목록을 조회합니다"
    )
    @GetMapping("/basic")
    public Api<List<UserSimpleResponse>> getBasicUsers() {
        List<UserSimpleResponse> users = userService.getBasicUsers();
        return Api.OK(users);
    }


    // =====================================================
    // 통계 (관리자 대시보드)
    // =====================================================

    /**
     * 사용자 통계 조회 (관리자 전용)
     */
    @Operation(
            summary = "사용자 통계 (관리자)",
            description = "전체 사용자 통계를 조회합니다"
    )
    @GetMapping("/statistics")
    public Api<Map<String, Object>> getUserStatistics() {
        long totalUsers = userService.getActiveUserCount();
        long basicCount = userService.getBasicUserCount();
        long vipCount = userService.getVipUserCount();
        Long totalRevenue = userService.getTotalRevenue();
        Double averageRevenue = userService.getAveragePurchaseAmount();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalUsers", totalUsers);
        statistics.put("basicUsers", basicCount);
        statistics.put("vipUsers", vipCount);
        statistics.put("totalRevenue", totalRevenue);
        statistics.put("averageRevenue", averageRevenue);

        return Api.OK(statistics);
    }
}
