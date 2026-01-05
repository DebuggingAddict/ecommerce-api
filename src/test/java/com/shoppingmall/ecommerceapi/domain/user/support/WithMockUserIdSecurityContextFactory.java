package com.shoppingmall.ecommerceapi.domain.user.support;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;

public class WithMockUserIdSecurityContextFactory implements WithSecurityContextFactory<WithMockUserId> {

    @Override
    public SecurityContext createSecurityContext(WithMockUserId annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Long principal = annotation.value(); // 핵심: Long principal
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        var auth = UsernamePasswordAuthenticationToken.authenticated(principal, "N/A", authorities);
        context.setAuthentication(auth);

        return context;
    }
}
