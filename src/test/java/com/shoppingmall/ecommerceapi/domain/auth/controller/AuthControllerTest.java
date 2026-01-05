package com.shoppingmall.ecommerceapi.domain.auth.controller;

import com.shoppingmall.ecommerceapi.common.security.config.SecurityConfig;
import com.shoppingmall.ecommerceapi.common.security.filter.JwtAuthenticationFilter;
import com.shoppingmall.ecommerceapi.domain.auth.dto.TokenResponse;
import com.shoppingmall.ecommerceapi.domain.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean
    AuthService authService;

    private UsernamePasswordAuthenticationToken authWithUserId(long userId) {
        // authentication(principal=Long)로 @AuthenticationPrincipal Long userId 바인딩 유도
        return new UsernamePasswordAuthenticationToken(
                userId,
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    @DisplayName("POST /api/auth/refresh: 성공 시 result=OK + body 토큰 필드까지 내려온다")
    void refreshToken_success() throws Exception {
        // given
        String requestJson = """
            { "refreshToken": "refresh-token-123" }
            """;

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("new-access")
                .refreshToken("new-refresh")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .build();

        given(authService.refreshAccessToken(
                eq("refresh-token-123"),
                anyString(),
                eq("old-access-token")
        )).willReturn(tokenResponse);

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf()) // 403 방지
                        .with(authentication(authWithUserId(1L)))    // 302 리다이렉트 방지(인증 주입)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer old-access-token")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0)")
                        .with(request -> {
                            ((MockHttpServletRequest) request).setRemoteAddr("127.0.0.1");
                            return request;
                        })
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.code").value(200))
                .andExpect(jsonPath("$.result.message").value("OK"))
                .andExpect(jsonPath("$.result.description").value("성공"))
                .andExpect(jsonPath("$.body.accessToken").value("new-access"))
                .andExpect(jsonPath("$.body.refreshToken").value("new-refresh"))
                .andExpect(jsonPath("$.body.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.body.expiresIn").value(3600));

        then(authService).should().refreshAccessToken(eq("refresh-token-123"), anyString(), eq("old-access-token"));
    }

    @Test
    @DisplayName("POST /api/auth/refresh: refreshToken이 공백이면 400")
    void refreshToken_validationFail_blankRefreshToken() throws Exception {
        // @NotBlank 검증으로 400 기대
        String requestJson = """
            { "refreshToken": "" }
            """;

        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf()) // 403 방지
                        .with(authentication(authWithUserId(1L))) // 인증 주입 
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/logout: 성공 시 body=null + authService.logout 호출")
    void logout_success() throws Exception {
        // given
        String requestJson = """
            { "refreshToken": "refresh-token-123" }
            """;

        // when & then
        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf()) // 403 방지
                        .with(authentication(authWithUserId(1L))) // 인증 주입 
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer access-token-abc")
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.code").value(200))
                .andExpect(jsonPath("$.result.message").value("OK"))
                .andExpect(jsonPath("$.result.description").value("성공"))
                .andExpect(jsonPath("$.body").isEmpty());

        then(authService).should().logout(eq(1L), eq("refresh-token-123"), eq("access-token-abc"));
    }

    @Test
    @DisplayName("POST /api/auth/logout/all: 성공 시 body=null + authService.logoutAllDevices 호출")
    void logoutAllDevices_success() throws Exception {
        mockMvc.perform(post("/api/auth/logout/all")
                        .with(csrf()) // 403 방지
                        .with(authentication(authWithUserId(1L)))) // 인증 주입
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.code").value(200))
                .andExpect(jsonPath("$.result.message").value("OK"))
                .andExpect(jsonPath("$.result.description").value("성공"))
                .andExpect(jsonPath("$.body").isEmpty());

        then(authService).should().logoutAllDevices(eq(1L));
    }

    @Test
    @DisplayName("GET /api/auth/validate: 성공 시 body=null")
    void validateToken_success() throws Exception {
        mockMvc.perform(get("/api/auth/validate")
                        .with(csrf()) // 403 방지
                        .with(authentication(authWithUserId(1L)))) // 인증 주입
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.code").value(200))
                .andExpect(jsonPath("$.result.message").value("OK"))
                .andExpect(jsonPath("$.result.description").value("성공"))
                .andExpect(jsonPath("$.body").isEmpty());
    }
}
