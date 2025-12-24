package com.shoppingmall.ecommerceapi.common.config.jpa;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "com.shoppingmall.ecommerceapi")
@EnableJpaRepositories(basePackages = "com.shoppingmall.ecommerceapi" )
public class JpaConfig {
}
