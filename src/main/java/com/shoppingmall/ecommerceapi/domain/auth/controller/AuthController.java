package com.shoppingmall.ecommerceapi.domain.auth.controller;

import com.shoppingmall.ecommerceapi.common.api.Api;
import com.shoppingmall.ecommerceapi.domain.auth.dto.RefreshRequest;
import com.shoppingmall.ecommerceapi.domain.auth.dto.TokenResponse;
import com.shoppingmall.ecommerceapi.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     *  Access Token 재발급 (기존 토큰 만료 처리 포함)
     */
    @Operation(
            summary = "Access Token 재발급",
            description = "Refresh Token을 이용하여 새로운 Access Token을 발급받습니다. " +
                    "기존 Access Token은 블랙리스트에 추가되어 즉시 만료됩니다." +
                    """
                    ⚠️ 주의사항:
                    1. 이 API 호출 후 새로 발급된 Access Token을 복사하세요.
                    2. Swagger 우측 상단 'Authorize' 버튼을 클릭하세요.
                    3. 기존 토큰을 지우고 새 토큰을 입력하세요.
                    4. 'Authorize' 버튼을 다시 클릭하여 인증을 갱신하세요.
        
                    이 과정을 거치지 않으면 기존 토큰이 블랙리스트에 추가되어 다른 API 호출이 실패합니다.
                    """
    )
    @PostMapping("/refresh")
    public Api<TokenResponse> refreshToken(
            @Valid @RequestBody RefreshRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest httpRequest) {

        // 디바이스 정보 추출
        String deviceInfo = extractDeviceInfo(httpRequest);

        //  Authorization 헤더에서 기존 Access Token 추출
        String oldAccessToken = extractAccessToken(authorization);

        //  Access Token 재발급 (기존 토큰 블랙리스트 추가)
        TokenResponse tokenResponse = authService.refreshAccessToken(
                request.getRefreshToken(),
                deviceInfo,
                oldAccessToken
        );

        log.info("Access Token 재발급 완료");
        return Api.OK(tokenResponse);
    }

    /**
     *  로그아웃 (Access Token 블랙리스트 추가)
     */
    @Operation(
            summary = "로그아웃",
            description = "현재 디바이스에서 로그아웃합니다. " +
                    "Access Token은 블랙리스트에 추가되어 즉시 무효화되고, Refresh Token은 삭제됩니다."
    )
    @PostMapping("/logout")
    public Api<Void> logout(
            @AuthenticationPrincipal Long userId,
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody RefreshRequest request) {

        //  Access Token 추출
        String accessToken = extractAccessToken(authorization);

        //  로그아웃 (Access Token 블랙리스트 추가 + Refresh Token 삭제)
        authService.logout(userId, request.getRefreshToken(), accessToken);

        log.info("로그아웃 완료: userId={}", userId);
        return Api.OK(null);
    }

    /**
     * 전체 디바이스 로그아웃
     */
    @Operation(
            summary = "전체 디바이스 로그아웃",
            description = "모든 디바이스에서 로그아웃합니다. " +
                    "모든 Refresh Token이 삭제되어 재발급이 불가능합니다."
    )
    @PostMapping("/logout/all")
    public Api<Void> logoutAllDevices(@AuthenticationPrincipal Long userId) {

        authService.logoutAllDevices(userId);

        log.info("전체 디바이스 로그아웃 완료: userId={}", userId);
        return Api.OK(null);
    }

    /**
     * 토큰 검증
     */
    @Operation(
            summary = "토큰 검증",
            description = "현재 Access Token이 유효한지 확인합니다. " +
                    "블랙리스트에 등록된 토큰은 유효하지 않은 것으로 처리됩니다."
    )
    @GetMapping("/validate")
    public Api<Void> validateToken(@AuthenticationPrincipal Long userId) {
        // JWT 필터를 통과했다면 유효한 토큰 (블랙리스트 체크 포함)
        log.debug("토큰 검증 성공: userId={}", userId);
        return Api.OK(null);
    }

    /**
     *  Authorization 헤더에서 Access Token 추출
     */
    private String extractAccessToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            log.debug("Access Token 추출 완료: {}...", token.substring(0, Math.min(10, token.length())));
            return token;
        }

        log.debug("Authorization 헤더에 Bearer Token 없음");
        return null;
    }

    /**
     * 디바이스 정보 추출
     */
    private String extractDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = request.getRemoteAddr();
        return String.format("%s (%s)", parseDeviceType(userAgent), ipAddress);
    }

    /**
     * User-Agent에서 디바이스 타입 파싱
     */
    private String parseDeviceType(String userAgent) {
        if (userAgent == null) return "UNKNOWN";
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile")) return "MOBILE";
        if (ua.contains("tablet")) return "TABLET";
        return "WEB";
    }
}
