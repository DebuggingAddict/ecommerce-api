package com.shoppingmall.ecommerceapi.common.aop;

import com.shoppingmall.ecommerceapi.common.api.Api;
import com.shoppingmall.ecommerceapi.common.api.Result;
import com.shoppingmall.ecommerceapi.common.code.CommonResultCode;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return Api.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        if (!(body instanceof Api<?> apiBody)) return body;

        HttpServletResponse servletResponse =
                ((ServletServerHttpResponse) response).getServletResponse();

        Result result = apiBody.getResult();
        if (result == null || result.getCode() == null) return apiBody;

        // 핵심: Integer(201/200)로 비교
        if (result.getCode().equals(CommonResultCode.CREATED.getCode())) {
            servletResponse.setStatus(HttpStatus.CREATED.value()); // 201
        } else if (result.getCode().equals(CommonResultCode.OK.getCode())) {
            servletResponse.setStatus(HttpStatus.OK.value());      // 200
        }

        return apiBody;
    }
}
