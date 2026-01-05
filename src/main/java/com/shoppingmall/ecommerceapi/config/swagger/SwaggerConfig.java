package com.shoppingmall.ecommerceapi.config.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

  @Bean
  public ModelResolver modelResolver(ObjectMapper objectMapper) {
    return new ModelResolver(objectMapper);
  }

  @Bean
  public OpenAPI openAPI() {
    // JWT Security Scheme 정의
    SecurityScheme securityScheme = new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .in(SecurityScheme.In.HEADER)
            .name("Authorization")
            .description("JWT Access Token을 입력하세요 (Bearer 제외)");

    // Security Requirement
    SecurityRequirement securityRequirement = new SecurityRequirement()
            .addList("Bearer Authentication");

    return new OpenAPI()
            .info(new Info()
                    .title("E-commerce API")
                    .description("""
                            ## 쇼핑몰 백엔드 API 문서
                            
                            ### 인증 방법
                            1. OAuth2 로그인: `http://localhost:8080/oauth2/authorization/google`
                            2. 리다이렉트된 URL에서 `accessToken` 파라미터 복사
                            3. 우측 상단 **Authorize** 버튼 클릭
                            4. Access Token 입력 (Bearer 제외)
                            5. **Authorize** 클릭
                            
                            ### API 테스트 흐름
                            1. `/api/auth/validate` - 토큰 검증
                            2. `/api/users/me` - 내 정보 조회
                            3. `/api/auth/refresh` - 토큰 갱신
                            4. `/api/auth/logout` - 로그아웃
                            """)
                    .version("v1.0.0"))
            .servers(List.of(
                    new Server()
                            .url("http://localhost:8080")
                            .description("로컬 개발 서버")
            ))
            .components(new Components()
                    .addSecuritySchemes("Bearer Authentication", securityScheme))
            .addSecurityItem(securityRequirement);
  }
}
