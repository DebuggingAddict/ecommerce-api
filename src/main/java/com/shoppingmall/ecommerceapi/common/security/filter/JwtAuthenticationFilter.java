package com.shoppingmall.ecommerceapi.common.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.ecommerceapi.common.api.Api;
import com.shoppingmall.ecommerceapi.common.code.CommonErrorCode;
import com.shoppingmall.ecommerceapi.common.security.util.JwtTokenProvider;
import com.shoppingmall.ecommerceapi.domain.auth.service.TokenBlacklistService;
import com.shoppingmall.ecommerceapi.domain.user.entity.enums.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService; // ⭐ 추가
    private final ObjectMapper objectMapper; // ⭐ 추가

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = resolveToken(request);

            if (token != null) {
                // 1. 토큰 유효성 검증
                if (!jwtTokenProvider.validateToken(token)) {
                    log.debug("유효하지 않은 JWT 토큰: {}", maskToken(token));
                    sendErrorResponse(response, CommonErrorCode.INVALID_TOKEN);
                    return;
                }

                // 2.  블랙리스트 확인
                if (tokenBlacklistService.isBlacklisted(token)) {
                    log.warn("블랙리스트에 등록된 토큰 접근 시도: {}", maskToken(token));
                    sendErrorResponse(response, CommonErrorCode.TOKEN_EXPIRED);
                    return;
                }

                // 3. 토큰에서 사용자 정보 추출
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                UserRole role = jwtTokenProvider.getRoleFromToken(token);

                // ROLE_ 접두사 추가 (Spring Security 규칙)
                String authority = "ROLE_" + role.name();

                // Authentication 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                List.of(new SimpleGrantedAuthority(authority))
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT 인증 성공: userId={}, role={}", userId, role);
            }
        } catch (Exception e) {
            log.error("JWT 인증 처리 중 오류 발생: {}", e.getMessage(), e);
            sendErrorResponse(response, CommonErrorCode.UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Request Header에서 토큰 추출
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     *  에러 응답 전송 (Api.ERROR 형식)
     */
    private void sendErrorResponse(HttpServletResponse response, CommonErrorCode errorCode) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(errorCode.getHttpStatus());

        Api<?> errorResponse = Api.ERROR(errorCode);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     *  토큰 마스킹 (로그용 - 보안)
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 10) + "...";
    }
}
