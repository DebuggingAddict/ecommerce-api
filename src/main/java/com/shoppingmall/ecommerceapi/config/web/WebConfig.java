package com.shoppingmall.ecommerceapi.config.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins("*") // 테스트를 위해 우선 모두 허용
        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
  }
}