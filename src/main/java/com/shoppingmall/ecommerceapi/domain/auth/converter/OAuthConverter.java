package com.shoppingmall.ecommerceapi.domain.auth.converter;

import com.shoppingmall.ecommerceapi.domain.auth.exception.AuthErrorCode;
import com.shoppingmall.ecommerceapi.domain.user.entity.User;
import com.shoppingmall.ecommerceapi.domain.user.entity.enums.UserGrade;
import com.shoppingmall.ecommerceapi.common.exception.BusinessException;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OAuthConverter {

    /**
     * OAuth2 제공자별 사용자 정보 추출
     */
    public OAuthUserInfo extractUserInfo(String provider, Map<String, Object> attributes) {
        if ("google".equals(provider)) {
            return extractGoogleUserInfo(attributes);
        }
        // 추후 kakao, naver 등 추가 가능

        throw new BusinessException(AuthErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
    }

    /**
     * Google OAuth2 사용자 정보 추출
     */
    private OAuthUserInfo extractGoogleUserInfo(Map<String, Object> attributes) {
        return OAuthUserInfo.builder()
                .email((String) attributes.get("email"))
                .name((String) attributes.get("name"))
                .providerId((String) attributes.get("sub"))
                .provider("google")
                .build();
    }

    /**
     * OAuth 정보로 신규 User 엔티티 생성 (OAuth 정보 제외)
     */
    public User toNewUser(OAuthUserInfo oAuthUserInfo) {
        return User.builder()
                .email(oAuthUserInfo.getEmail())
                .name(oAuthUserInfo.getName())
                .grade(UserGrade.BASIC)
                .build();
    }

    /**
     * OAuth 사용자 정보 DTO
     */
    @Getter
    @lombok.Builder
    public static class OAuthUserInfo {
        private String email;
        private String name;
        private String providerId;
        private String provider;
    }
}
