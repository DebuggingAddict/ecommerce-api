package com.shoppingmall.ecommerceapi.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.ecommerceapi.common.security.filter.JwtAuthenticationFilter;
import com.shoppingmall.ecommerceapi.domain.user.dto.UserProfileResponse;
import com.shoppingmall.ecommerceapi.domain.user.dto.UserProfileUpdateRequest;
import com.shoppingmall.ecommerceapi.domain.user.service.UserService;

import com.shoppingmall.ecommerceapi.domain.user.support.WithMockUserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean UserService userService;

    // =========================
    // GET /api/users/me
    // =========================

    @Test
    @DisplayName("GET /api/users/me: 인증(principal=Long)이면 200 + 프로필")
    @WithMockUserId(1L)
    void getMyProfile_success() throws Exception {
        given(userService.getMyProfile(1L)).willReturn(
                UserProfileResponse.builder()
                        .id(1L)
                        .email("me@test.com")
                        .name("me")
                        .build()
        );

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.code").value(200))
                .andExpect(jsonPath("$.body.id").value(1))
                .andExpect(jsonPath("$.body.email").value("me@test.com"));

        then(userService).should().getMyProfile(1L);
    }

    @Test
    @DisplayName("GET /api/users/me: 인증 없으면 302 + 서비스 호출 없음")
    void getMyProfile_unauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().is3xxRedirection());

        then(userService).should(never()).getMyProfile(1L);
    }

    // =========================
    // PUT /api/users/me
    // =========================

    @Test
    @DisplayName("PUT /api/users/me: 성공(200) + 서비스 호출 인자 검증")
    @WithMockUserId(1L)
    void updateProfile_success() throws Exception {
        var body = UserProfileUpdateRequest.builder()
                .name("newName")
                .phone("010-1234-5678")
                .zipCode(null)
                .address("Seoul")
                .addressDetail(null)
                .build();

        // 서비스는 어떤 request가 오든 응답만 돌려주게 stub (인자는 captor로 검증)
        given(userService.updateProfile(org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(UserProfileUpdateRequest.class)))
                .willReturn(null); // 지금 응답 body가 null로 내려가니까 일단 null

        mockMvc.perform(put("/api/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.code").value(200));

        ArgumentCaptor<UserProfileUpdateRequest> captor =
                ArgumentCaptor.forClass(UserProfileUpdateRequest.class);

        then(userService).should().updateProfile(org.mockito.ArgumentMatchers.eq(1L), captor.capture());

        UserProfileUpdateRequest actual = captor.getValue();
        assertThat(actual.getName()).isEqualTo("newName");
        assertThat(actual.getPhone()).isEqualTo("010-1234-5678");
        assertThat(actual.getAddress()).isEqualTo("Seoul");
        assertThat(actual.getZipCode()).isNull();
        assertThat(actual.getAddressDetail()).isNull();
    }

    @Test
    @DisplayName("PUT /api/users/me: 인증은 있지만 CSRF 없으면 403 + 서비스 호출 없음")
    @WithMockUserId(1L)
    void updateProfile_forbidden_noCsrf() throws Exception {
        var req = UserProfileUpdateRequest.builder()
                .name("newName")
                .phone("01012345678")
                .address("Seoul")
                .build();

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());

        then(userService).should(never()).updateProfile(1L, req);
    }

    @Test
    @DisplayName("PUT /api/users/me: 인증 없으면 302 + 서비스 호출 없음")
    void updateProfile_unauthorized() throws Exception {
        var req = UserProfileUpdateRequest.builder()
                .name("newName")
                .phone("01012345678")
                .address("Seoul")
                .build();

        mockMvc.perform(put("/api/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is3xxRedirection());

        then(userService).should(never()).updateProfile(1L, req);
    }

    // =========================
    // DELETE /api/users/me
    // =========================

    @Test
    @DisplayName("DELETE /api/users/me: 인증(principal=Long)이면 200 + message, 서비스 호출")
    @WithMockUserId(1L)
    void deactivateAccount_success() throws Exception {
        mockMvc.perform(delete("/api/users/me").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.code").value(200))
                .andExpect(jsonPath("$.body.message").value("회원 탈퇴가 완료되었습니다"));

        then(userService).should().deactivateAccount(1L);
    }

    @Test
    @DisplayName("DELETE /api/users/me: 인증은 있지만 CSRF 없으면 403 + 서비스 호출 없음")
    @WithMockUserId(1L)
    void deactivateAccount_forbidden_noCsrf() throws Exception {
        mockMvc.perform(delete("/api/users/me"))
                .andExpect(status().isForbidden());

        then(userService).should(never()).deactivateAccount(1L);
    }

    @Test
    @DisplayName("DELETE /api/users/me: 인증 없으면 302 + 서비스 호출 없음")
    void deactivateAccount_unauthorized() throws Exception {
        mockMvc.perform(delete("/api/users/me").with(csrf()))
                .andExpect(status().is3xxRedirection());

        then(userService).should(never()).deactivateAccount(1L);
    }
}
