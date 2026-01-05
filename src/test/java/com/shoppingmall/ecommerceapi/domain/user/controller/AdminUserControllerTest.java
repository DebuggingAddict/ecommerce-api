package com.shoppingmall.ecommerceapi.domain.user.controller;

import com.shoppingmall.ecommerceapi.common.security.filter.JwtAuthenticationFilter;
import com.shoppingmall.ecommerceapi.domain.user.dto.UserSimpleResponse;
import com.shoppingmall.ecommerceapi.domain.user.entity.enums.UserGrade;
import com.shoppingmall.ecommerceapi.domain.user.entity.enums.UserRole;
import com.shoppingmall.ecommerceapi.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AdminUserController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@Import(AdminUserControllerTest.TestSecurityConfig.class)
class AdminUserControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean
    UserService userService;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> {})
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/admin/**").hasRole("ADMIN")
                            .anyRequest().permitAll()
                    )
                    .build();
        }
    }

    // =====================================================
    // 계정 관리
    // =====================================================

    @Test
    @DisplayName("POST /api/admin/users/{userId}/activate: ADMIN이면 200 + message")
    @WithMockUser(roles = "ADMIN")
    void activateAccount_success_admin() throws Exception {
        mockMvc.perform(post("/api/admin/users/{userId}/activate", 1L).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.code").value(200))
                .andExpect(jsonPath("$.body.message").value("계정이 복구되었습니다"));

        then(userService).should().activateAccount(1L);
    }

    @Test
    @DisplayName("POST /api/admin/users/{userId}/activate: USER면 403")
    @WithMockUser(roles = "USER")
    void activateAccount_forbidden_user() throws Exception {
        mockMvc.perform(post("/api/admin/users/{userId}/activate", 1L).with(csrf()))
                .andExpect(status().isForbidden());

        then(userService).should(never()).activateAccount(1L);
    }

    @Test
    @DisplayName("PUT /api/admin/users/{userId}/grade: USER면 403")
    @WithMockUser(roles = "USER")
    void updateGrade_forbidden_user() throws Exception {
        mockMvc.perform(put("/api/admin/users/{userId}/grade", 1L)
                        .with(csrf())
                        .queryParam("grade", "VIP"))
                .andExpect(status().isForbidden());

        then(userService).should(never()).updateGrade(1L, UserGrade.VIP);
    }

    // =====================================================
    // 등급/역할 관리
    // =====================================================

    @Test
    @DisplayName("PUT /api/admin/users/{userId}/grade?grade=VIP: ADMIN이면 200 + grade")
    @WithMockUser(roles = "ADMIN")
    void updateGrade_success_admin() throws Exception {
        mockMvc.perform(put("/api/admin/users/{userId}/grade", 1L)
                        .with(csrf())
                        .queryParam("grade", "VIP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.code").value(200))
                .andExpect(jsonPath("$.body.message").value("등급이 변경되었습니다"))
                .andExpect(jsonPath("$.body.grade").value("VIP"));

        then(userService).should().updateGrade(1L, UserGrade.VIP);
    }

    @Test
    @DisplayName("PUT /api/admin/users/{userId}/role?role=ADMIN: ADMIN이면 200 + role")
    @WithMockUser(roles = "ADMIN")
    void updateRole_success_admin() throws Exception {
        mockMvc.perform(put("/api/admin/users/{userId}/role", 1L)
                        .with(csrf())
                        .queryParam("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.code").value(200))
                .andExpect(jsonPath("$.body.message").value("역할이 변경되었습니다"))
                .andExpect(jsonPath("$.body.role").value("ADMIN"));

        then(userService).should().updateRole(1L, UserRole.ADMIN);
    }

    @Test
    @DisplayName("PUT /api/admin/users/{userId}/role: USER면 403")
    @WithMockUser(roles = "USER")
    void updateRole_forbidden_user() throws Exception {
        mockMvc.perform(put("/api/admin/users/{userId}/role", 1L)
                        .with(csrf())
                        .queryParam("role", "ADMIN"))
                .andExpect(status().isForbidden());

        then(userService).should(never()).updateRole(1L, UserRole.ADMIN);
    }

    // =====================================================
    // 검색/필터링
    // =====================================================

    @Test
    @DisplayName("GET /api/admin/users/search/name?name=kim: ADMIN이면 200 + 리스트")
    @WithMockUser(roles = "ADMIN")
    void searchByName_success_admin() throws Exception {
        given(userService.searchByName("kim")).willReturn(List.of(
                UserSimpleResponse.builder().id(1L).email("a@test.com").name("kim").build(),
                UserSimpleResponse.builder().id(2L).email("b@test.com").name("kim2").build()
        ));

        mockMvc.perform(get("/api/admin/users/search/name")
                        .queryParam("name", "kim"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.code").value(200))
                .andExpect(jsonPath("$.body.length()").value(2))
                .andExpect(jsonPath("$.body[0].id").value(1))
                .andExpect(jsonPath("$.body[1].id").value(2));

        then(userService).should().searchByName("kim");
    }

    @Test
    @DisplayName("GET /api/admin/users/search/email?email=test: ADMIN이면 200 + 리스트")
    @WithMockUser(roles = "ADMIN")
    void searchByEmail_success_admin() throws Exception {
        given(userService.searchByEmail("test")).willReturn(List.of(
                UserSimpleResponse.builder().id(3L).email("test@test.com").name("t").build()
        ));

        mockMvc.perform(get("/api/admin/users/search/email")
                        .queryParam("email", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.code").value(200))
                .andExpect(jsonPath("$.body.length()").value(1))
                .andExpect(jsonPath("$.body[0].email").value("test@test.com"));

        then(userService).should().searchByEmail("test");
    }

    @Test
    @DisplayName("GET /api/admin/users/grade/{grade}: ADMIN이면 200 + 리스트")
    @WithMockUser(roles = "ADMIN")
    void getUsersByGrade_success_admin() throws Exception {
        given(userService.getUsersByGrade(UserGrade.VIP)).willReturn(List.of(
                UserSimpleResponse.builder().id(10L).email("vip@test.com").name("vip").grade(UserGrade.VIP).build()
        ));

        mockMvc.perform(get("/api/admin/users/grade/{grade}", "VIP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.code").value(200))
                .andExpect(jsonPath("$.body[0].id").value(10));

        then(userService).should().getUsersByGrade(UserGrade.VIP);
    }

    @Test
    @DisplayName("GET /api/admin/users/vip: ADMIN이면 200 + 리스트")
    @WithMockUser(roles = "ADMIN")
    void getVipUsers_success_admin() throws Exception {
        given(userService.getVipUsers()).willReturn(List.of(
                UserSimpleResponse.builder().id(11L).email("vip2@test.com").name("vip2").grade(UserGrade.VIP).build()
        ));

        mockMvc.perform(get("/api/admin/users/vip"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.code").value(200))
                .andExpect(jsonPath("$.body[0].id").value(11));

        then(userService).should().getVipUsers();
    }

    @Test
    @DisplayName("GET /api/admin/users/basic: ADMIN이면 200 + 리스트")
    @WithMockUser(roles = "ADMIN")
    void getBasicUsers_success_admin() throws Exception {
        given(userService.getBasicUsers()).willReturn(List.of(
                UserSimpleResponse.builder().id(12L).email("b@test.com").name("basic").grade(UserGrade.BASIC).build()
        ));

        mockMvc.perform(get("/api/admin/users/basic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.code").value(200))
                .andExpect(jsonPath("$.body[0].id").value(12));

        then(userService).should().getBasicUsers();
    }
    @Test
    @DisplayName("GET /api/admin/users/search/name: USER면 403")
    @WithMockUser(roles = "USER")
    void searchByName_forbidden_user() throws Exception {
        mockMvc.perform(get("/api/admin/users/search/name")
                        .queryParam("name", "kim"))
                .andExpect(status().isForbidden());

        then(userService).should(never()).searchByName("kim");
    }

    @Test
    @DisplayName("GET /api/admin/users/search/email: USER면 403")
    @WithMockUser(roles = "USER")
    void searchByEmail_forbidden_user() throws Exception {
        mockMvc.perform(get("/api/admin/users/search/email")
                        .queryParam("email", "test"))
                .andExpect(status().isForbidden());

        then(userService).should(never()).searchByEmail("test");
    }

    @Test
    @DisplayName("GET /api/admin/users/grade/{grade}: USER면 403")
    @WithMockUser(roles = "USER")
    void getUsersByGrade_forbidden_user() throws Exception {
        mockMvc.perform(get("/api/admin/users/grade/{grade}", "VIP"))
                .andExpect(status().isForbidden());

        then(userService).should(never()).getUsersByGrade(UserGrade.VIP);
    }

    @Test
    @DisplayName("GET /api/admin/users/vip: USER면 403")
    @WithMockUser(roles = "USER")
    void getVipUsers_forbidden_user() throws Exception {
        mockMvc.perform(get("/api/admin/users/vip"))
                .andExpect(status().isForbidden());

        then(userService).should(never()).getVipUsers();
    }

    @Test
    @DisplayName("GET /api/admin/users/basic: USER면 403")
    @WithMockUser(roles = "USER")
    void getBasicUsers_forbidden_user() throws Exception {
        mockMvc.perform(get("/api/admin/users/basic"))
                .andExpect(status().isForbidden());

        then(userService).should(never()).getBasicUsers();
    }

    // =====================================================
    // 통계
    // =====================================================

    @Test
    @DisplayName("GET /api/admin/users/statistics: ADMIN이면 200 + 통계 응답")
    @WithMockUser(roles = "ADMIN")
    void getUserStatistics_success_admin() throws Exception {
        given(userService.getActiveUserCount()).willReturn(100L);
        given(userService.getBasicUserCount()).willReturn(80L);
        given(userService.getVipUserCount()).willReturn(20L);
        given(userService.getTotalRevenue()).willReturn(123456L);
        given(userService.getAveragePurchaseAmount()).willReturn(1234.5);

        mockMvc.perform(get("/api/admin/users/statistics")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.code").value(200))
                .andExpect(jsonPath("$.body.totalUsers").value(100))
                .andExpect(jsonPath("$.body.basicUsers").value(80))
                .andExpect(jsonPath("$.body.vipUsers").value(20))
                .andExpect(jsonPath("$.body.totalRevenue").value(123456))
                .andExpect(jsonPath("$.body.averageRevenue").value(1234.5));

        then(userService).should().getActiveUserCount();
        then(userService).should().getBasicUserCount();
        then(userService).should().getVipUserCount();
        then(userService).should().getTotalRevenue();
        then(userService).should().getAveragePurchaseAmount();
    }
    @Test
    @DisplayName("GET /api/admin/users/statistics: USER면 403")
    @WithMockUser(roles = "USER")
    void getUserStatistics_forbidden_user() throws Exception {
        mockMvc.perform(get("/api/admin/users/statistics")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        then(userService).should(never()).getActiveUserCount();
        then(userService).should(never()).getBasicUserCount();
        then(userService).should(never()).getVipUserCount();
        then(userService).should(never()).getTotalRevenue();
        then(userService).should(never()).getAveragePurchaseAmount();
    }
}
