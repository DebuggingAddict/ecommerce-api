package com.shoppingmall.ecommerceapi.common.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.ecommerceapi.common.api.Api;
import com.shoppingmall.ecommerceapi.common.code.CommonErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증 실패 시 (401) - Security Filter 단계
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        log.warn("Unauthorized access attempt: {}", authException.getMessage());

        // ⭐ 공통 에러코드 사용
        CommonErrorCode errorCode = CommonErrorCode.UNAUTHORIZED;

        Api<?> errorResponse = Api.ERROR(errorCode,"인증확인");

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(errorCode.getHttpStatus());

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
