package com.shoppingmall.ecommerceapi.common.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.ecommerceapi.common.code.CommonErrorCode;
import com.shoppingmall.ecommerceapi.common.api.Api;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.warn("Access denied for URI: {}", request.getRequestURI());


        CommonErrorCode errorCode = request.getRequestURI().contains("/admin")
                ? CommonErrorCode.ADMIN_REQUIRED
                : CommonErrorCode.FORBIDDEN;

        Api<?> errorResponse = Api.ERROR(errorCode,"ADMIN 권한 없음");

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(errorCode.getHttpStatus());

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
