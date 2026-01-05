package com.shoppingmall.ecommerceapi.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

@Getter
@Builder
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;

    @Value("${token.access-token.plus-hour}")
    static Long EXPIRATION_TIME; // 1시간 (밀리초 단위)


    public static TokenResponse of(String accessToken, String refreshToken) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(EXPIRATION_TIME * 3600L) // 1시간 (초 단위)
                .build();
    }
}
