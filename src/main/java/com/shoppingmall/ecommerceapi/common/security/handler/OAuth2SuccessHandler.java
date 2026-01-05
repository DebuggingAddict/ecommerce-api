package com.shoppingmall.ecommerceapi.common.security.handler;

import com.shoppingmall.ecommerceapi.domain.auth.service.AuthService;
import com.shoppingmall.ecommerceapi.domain.user.entity.User;
import com.shoppingmall.ecommerceapi.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    private final UserRepository userRepository;

    @Value("${oauth2.redirect-url}")
    private String redirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Long userId = Long.parseLong(oAuth2User.getName()); // CustomOAuth2User의 getName()

        // User 조회하여 role 가져오기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

        // JWT 토큰 발급 (role 포함)
        String deviceInfo = extractDeviceInfo(request);
        var tokenResponse = authService.generateTokens(userId, user.getRole(), deviceInfo);

        log.info("OAuth2 로그인 성공: userId={}, role={}", userId, user.getRole());

        // Swagger로 리다이렉트 (토큰 쿼리 파라미터로 전달)
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                .queryParam("accessToken", tokenResponse.getAccessToken())
                .queryParam("refreshToken", tokenResponse.getRefreshToken())
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
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
