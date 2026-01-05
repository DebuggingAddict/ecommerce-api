package com.shoppingmall.ecommerceapi.domain.user.service;

import com.shoppingmall.ecommerceapi.common.exception.BusinessException;
import com.shoppingmall.ecommerceapi.domain.user.converter.UserConverter;
import com.shoppingmall.ecommerceapi.domain.user.dto.UserProfileResponse;
import com.shoppingmall.ecommerceapi.domain.user.dto.UserProfileUpdateRequest;
import com.shoppingmall.ecommerceapi.domain.user.dto.UserSimpleResponse;
import com.shoppingmall.ecommerceapi.domain.user.entity.User;
import com.shoppingmall.ecommerceapi.domain.user.entity.enums.UserGrade;
import com.shoppingmall.ecommerceapi.domain.user.entity.enums.UserRole;
import com.shoppingmall.ecommerceapi.domain.user.exception.UserErrorCode;
import com.shoppingmall.ecommerceapi.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

class UserServiceTest {

    private UserRepository userRepository;
    private UserConverter userConverter;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userConverter = mock(UserConverter.class);
        userService = new UserService(userRepository, userConverter);
    }

    private User user(long id, UserGrade grade, long totalPurchaseAmount, boolean isActive, LocalDateTime deletedAt) {
        return User.builder()
                .id(id)
                .email("u" + id + "@test.com")
                .name("user" + id)
                .role(UserRole.USER)
                .grade(grade)
                .totalPurchaseAmount(totalPurchaseAmount)
                .isActive(isActive)
                .deletedAt(deletedAt)
                .build();
    }

    private UserProfileResponse profileResp(long id) {
        return UserProfileResponse.builder()
                .id(id)
                .email("u" + id + "@test.com")
                .name("user" + id)
                .role(UserRole.USER)
                .grade(UserGrade.BASIC)
                .totalPurchaseAmount(0L)
                .isActive(true)
                .build();
    }

    private UserSimpleResponse simpleResp(long id) {
        return UserSimpleResponse.builder()
                .id(id)
                .email("u" + id + "@test.com")
                .name("user" + id)
                .role(UserRole.USER)
                .grade(UserGrade.BASIC)
                .totalPurchaseAmount(0L)
                .isActive(true)
                .build();
    }

    // =====================================================
    // 프로필 조회
    // =====================================================

    @Test
    @DisplayName("getMyProfile: 활성 사용자면 profileResponse 반환")
    void getMyProfile_success() {
        User u = user(1L, UserGrade.BASIC, 0, true, null);
        UserProfileResponse resp = profileResp(1L);

        given(userRepository.findActiveById(1L)).willReturn(Optional.of(u));
        given(userConverter.toProfileResponse(u)).willReturn(resp);

        UserProfileResponse result = userService.getMyProfile(1L);

        assertThat(result.getId()).isEqualTo(1L);
        then(userRepository).should().findActiveById(1L);
        then(userConverter).should().toProfileResponse(u);
    }

    @Test
    @DisplayName("getMyProfile: 비활성/삭제면 USER_NOT_ACTIVE 예외")
    void getMyProfile_fail_userNotActive() {
        given(userRepository.findActiveById(1L)).willReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.getMyProfile(1L)); // assertThrows [web:236]
        assertThat(ex.getCode()).isEqualTo(UserErrorCode.USER_NOT_ACTIVE);
    }

    @Test
    @DisplayName("getUserPublicInfo: 존재하면 publicResponse 반환")
    void getUserPublicInfo_success() {
        User u = user(2L, UserGrade.BASIC, 0, true, null);
        UserProfileResponse resp = profileResp(2L);

        given(userRepository.findById(2L)).willReturn(Optional.of(u));
        given(userConverter.toPublicResponse(u)).willReturn(resp);

        UserProfileResponse result = userService.getUserPublicInfo(2L);

        assertThat(result.getId()).isEqualTo(2L);
        then(userRepository).should().findById(2L);
        then(userConverter).should().toPublicResponse(u);
    }

    @Test
    @DisplayName("getUserPublicInfo: 없으면 USER_NOT_FOUND 예외")
    void getUserPublicInfo_fail_userNotFound() {
        given(userRepository.findById(2L)).willReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.getUserPublicInfo(2L));
        assertThat(ex.getCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("getUserSimpleInfo: 존재하면 simpleResponse 반환")
    void getUserSimpleInfo_success() {
        User u = user(3L, UserGrade.BASIC, 0, true, null);
        UserSimpleResponse resp = simpleResp(3L);

        given(userRepository.findById(3L)).willReturn(Optional.of(u));
        given(userConverter.toSimpleResponse(u)).willReturn(resp);

        UserSimpleResponse result = userService.getUserSimpleInfo(3L);

        assertThat(result.getId()).isEqualTo(3L);
        then(userRepository).should().findById(3L);
        then(userConverter).should().toSimpleResponse(u);
    }

    @Test
    @DisplayName("getUserSimpleInfo: 없으면 USER_NOT_FOUND 예외")
    void getUserSimpleInfo_fail_userNotFound() {
        given(userRepository.findById(3L)).willReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.getUserSimpleInfo(3L));
        assertThat(ex.getCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    // =====================================================
    // 프로필 수정
    // =====================================================

    @Test
    @DisplayName("updateProfile: 활성 사용자면 엔티티 수정 후 profileResponse 반환")
    void updateProfile_success() {
        User u = user(1L, UserGrade.BASIC, 0, true, null);

        UserProfileUpdateRequest req = new UserProfileUpdateRequest(
                "새이름",
                "010-1234-5678",
                "12345",
                "서울시 강남구 테헤란로 123",
                "101동 101호"
        );

        UserProfileResponse resp = UserProfileResponse.builder()
                .id(1L)
                .name("새이름")
                .phone("010-1234-5678")
                .zipCode("12345")
                .address("서울시 강남구 테헤란로 123")
                .addressDetail("101동 101호")
                .build();

        given(userRepository.findActiveById(1L)).willReturn(Optional.of(u));
        given(userConverter.toProfileResponse(u)).willReturn(resp);

        UserProfileResponse result = userService.updateProfile(1L, req);

        assertThat(u.getName()).isEqualTo("새이름");
        assertThat(result.getName()).isEqualTo("새이름");
        then(userRepository).should().findActiveById(1L);
        then(userConverter).should().toProfileResponse(u);
    }

    @Test
    @DisplayName("updateProfile: 비활성/삭제면 USER_NOT_ACTIVE 예외")
    void updateProfile_fail_userNotActive() {
        given(userRepository.findActiveById(1L)).willReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.updateProfile(1L, new UserProfileUpdateRequest("홍길동", null, null, null, null)));
        assertThat(ex.getCode()).isEqualTo(UserErrorCode.USER_NOT_ACTIVE);

        then(userConverter).should(never()).toProfileResponse(any());
    }

    // =====================================================
    // 계정 관리
    // =====================================================

    @Test
    @DisplayName("deactivateAccount: 활성 사용자면 deactivate 수행")
    void deactivateAccount_success() {
        User u = user(1L, UserGrade.BASIC, 0, true, null);
        given(userRepository.findById(1L)).willReturn(Optional.of(u));

        assertDoesNotThrow(() -> userService.deactivateAccount(1L));

        assertThat(u.getIsActive()).isFalse();
        assertThat(u.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("deactivateAccount: 없으면 USER_NOT_FOUND 예외")
    void deactivateAccount_fail_userNotFound() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.deactivateAccount(1L));
        assertThat(ex.getCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("deactivateAccount: 이미 탈퇴면 USER_ALREADY_DEACTIVATED 예외")
    void deactivateAccount_fail_alreadyDeactivated() {
        User u = user(1L, UserGrade.BASIC, 0, false, LocalDateTime.now());
        given(userRepository.findById(1L)).willReturn(Optional.of(u));

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.deactivateAccount(1L));
        assertThat(ex.getCode()).isEqualTo(UserErrorCode.USER_ALREADY_DEACTIVATED);
    }

    @Test
    @DisplayName("activateAccount: 비활성이면 activate 수행")
    void activateAccount_success() {
        User u = user(1L, UserGrade.BASIC, 0, false, LocalDateTime.now());
        given(userRepository.findById(1L)).willReturn(Optional.of(u));

        userService.activateAccount(1L);

        assertThat(u.getIsActive()).isTrue();
        assertThat(u.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("activateAccount: 없으면 USER_NOT_FOUND 예외")
    void activateAccount_fail_userNotFound() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.activateAccount(1L));
        assertThat(ex.getCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("activateAccount: 이미 활성화면 USER_ALREADY_ACTIVATED 예외")
    void activateAccount_fail_alreadyActivated() {
        User u = user(1L, UserGrade.BASIC, 0, true, null);
        given(userRepository.findById(1L)).willReturn(Optional.of(u));

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.activateAccount(1L));
        assertThat(ex.getCode()).isEqualTo(UserErrorCode.USER_ALREADY_ACTIVATED);
    }

    // =====================================================
    // 등급/역할 관리
    // =====================================================

    @Test
    @DisplayName("updateGrade: BASIC/VIP 외(null 포함)이면 INVALID_GRADE 예외 + repository 조회 안함")
    void updateGrade_fail_invalidGrade() {
        BusinessException ex = assertThrows(BusinessException.class, () -> userService.updateGrade(1L, null));
        assertThat(ex.getCode()).isEqualTo(UserErrorCode.INVALID_GRADE);

        then(userRepository).should(never()).findById(anyLong());
    }

    @Test
    @DisplayName("updateGrade: 사용자 존재하면 grade 변경")
    void updateGrade_success() {
        User u = user(1L, UserGrade.BASIC, 0, true, null);
        given(userRepository.findById(1L)).willReturn(Optional.of(u));

        userService.updateGrade(1L, UserGrade.VIP);

        assertThat(u.getGrade()).isEqualTo(UserGrade.VIP);
    }

    @Test
    @DisplayName("updateGrade: 사용자 없으면 USER_NOT_FOUND 예외")
    void updateGrade_fail_userNotFound() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.updateGrade(1L, UserGrade.BASIC));
        assertThat(ex.getCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("updateRole: 사용자 존재하면 role 변경")
    void updateRole_success() {
        User u = user(1L, UserGrade.BASIC, 0, true, null);
        given(userRepository.findById(1L)).willReturn(Optional.of(u));

        userService.updateRole(1L, UserRole.ADMIN);

        assertThat(u.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("updateRole: 사용자 없으면 USER_NOT_FOUND 예외")
    void updateRole_fail_userNotFound() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.updateRole(1L, UserRole.ADMIN));
        assertThat(ex.getCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    // =====================================================
    // 구매 관련
    // =====================================================

    @Test
    @DisplayName("addPurchaseAmount: 구매 금액 추가 + VIP 기준 이상이면 BASIC -> VIP 자동 승급")
    void addPurchaseAmount_autoUpgradeToVip() {
        User u = user(1L, UserGrade.BASIC, 4_999_000L, true, null);
        given(userRepository.findById(1L)).willReturn(Optional.of(u));

        userService.addPurchaseAmount(1L, 2_000L);

        assertThat(u.getTotalPurchaseAmount()).isEqualTo(5_001_000L);
        assertThat(u.getGrade()).isEqualTo(UserGrade.VIP);
    }

    @Test
    @DisplayName("addPurchaseAmount: 사용자 없으면 USER_NOT_FOUND 예외")
    void addPurchaseAmount_fail_userNotFound() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.addPurchaseAmount(1L, 1_000L));
        assertThat(ex.getCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    // =====================================================
    // 검색/필터링
    // =====================================================

    @Test
    @DisplayName("searchByName: users -> simpleResponse로 매핑")
    void searchByName_mapsToSimpleResponses() {
        User u1 = user(1L, UserGrade.BASIC, 0, true, null);
        User u2 = user(2L, UserGrade.BASIC, 0, true, null);

        UserSimpleResponse r1 = simpleResp(1L);
        UserSimpleResponse r2 = simpleResp(2L);

        given(userRepository.searchByName("kim")).willReturn(List.of(u1, u2));
        given(userConverter.toSimpleResponse(u1)).willReturn(r1);
        given(userConverter.toSimpleResponse(u2)).willReturn(r2);

        List<UserSimpleResponse> result = userService.searchByName("kim");

        assertThat(result).extracting(UserSimpleResponse::getId).containsExactly(1L, 2L);
        then(userRepository).should().searchByName("kim");
        then(userConverter).should().toSimpleResponse(u1);
        then(userConverter).should().toSimpleResponse(u2);
    }

    @Test
    @DisplayName("searchByEmail: users -> simpleResponse로 매핑")
    void searchByEmail_mapsToSimpleResponses() {
        User u1 = user(1L, UserGrade.BASIC, 0, true, null);
        UserSimpleResponse r1 = simpleResp(1L);

        given(userRepository.searchByEmail("test")).willReturn(List.of(u1));
        given(userConverter.toSimpleResponse(u1)).willReturn(r1);

        List<UserSimpleResponse> result = userService.searchByEmail("test");

        assertThat(result).extracting(UserSimpleResponse::getEmail).containsExactly("u1@test.com");
        then(userRepository).should().searchByEmail("test");
        then(userConverter).should().toSimpleResponse(u1);
    }

    // =====================================================
    // 통계
    // =====================================================

    @Test
    @DisplayName("getTotalRevenue/getAveragePurchaseAmount: null이면 0으로 보정")
    void stats_nullSafe() {
        given(userRepository.sumTotalPurchaseAmount()).willReturn(null);
        given(userRepository.averagePurchaseAmount()).willReturn(null);

        assertThat(userService.getTotalRevenue()).isEqualTo(0L);
        assertThat(userService.getAveragePurchaseAmount()).isEqualTo(0.0);
    }
}
