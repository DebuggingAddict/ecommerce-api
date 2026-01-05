package com.shoppingmall.ecommerceapi.domain.user.service;

import com.shoppingmall.ecommerceapi.common.exception.BusinessException;
import com.shoppingmall.ecommerceapi.domain.user.dto.UserSimpleResponse;
import com.shoppingmall.ecommerceapi.domain.user.exception.UserErrorCode;
import com.shoppingmall.ecommerceapi.domain.user.converter.UserConverter;
import com.shoppingmall.ecommerceapi.domain.user.dto.UserProfileResponse;
import com.shoppingmall.ecommerceapi.domain.user.dto.UserProfileUpdateRequest;
import com.shoppingmall.ecommerceapi.domain.user.entity.User;
import com.shoppingmall.ecommerceapi.domain.user.entity.enums.UserGrade;
import com.shoppingmall.ecommerceapi.domain.user.entity.enums.UserRole;
import com.shoppingmall.ecommerceapi.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자 프로필 관리 서비스 (User 도메인)
 * OAuth2 전용, BASIC/VIP 등급만 사용
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;

    // VIP 승급 기준 금액 (예: 500만원)
    private static final Long VIP_THRESHOLD = 5_000_000L;

    // =====================================================
    // 프로필 조회
    // =====================================================

    public UserProfileResponse getMyProfile(Long userId) {
        log.debug("프로필 조회 요청: userId={}", userId);

        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_ACTIVE));

        return userConverter.toProfileResponse(user);
    }

    public UserProfileResponse getUserPublicInfo(Long userId) {
        log.debug("공개 정보 조회: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        return userConverter.toPublicResponse(user);
    }

    public UserSimpleResponse getUserSimpleInfo(Long userId) {
        log.debug("간단 정보 조회: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        return userConverter.toSimpleResponse(user);
    }

    // =====================================================
    // 프로필 수정
    // =====================================================

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UserProfileUpdateRequest request) {
        log.info("프로필 수정 요청: userId={}", userId);

        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_ACTIVE));

        user.updateProfile(
                request.getName(),
                request.getPhone(),
                request.getZipCode(),
                request.getAddress(),
                request.getAddressDetail()
        );

        log.info("프로필 수정 완료: userId={}, name={}", userId, request.getName());

        return userConverter.toProfileResponse(user);
    }

    // =====================================================
    // 계정 관리
    // =====================================================

    @Transactional
    public void deactivateAccount(Long userId) {
        log.info("회원 탈퇴 요청: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (!user.getIsActive()) {
            throw new BusinessException(UserErrorCode.USER_ALREADY_DEACTIVATED);
        }

        user.deactivate();

        log.info("회원 탈퇴 완료: userId={}", userId);
    }

    @Transactional
    public void activateAccount(Long userId) {
        log.info("계정 복구 요청: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (user.getIsActive()) {
            throw new BusinessException(UserErrorCode.USER_ALREADY_ACTIVATED);
        }

        user.activate();

        log.info("계정 복구 완료: userId={}", userId);
    }

    // =====================================================
    // 등급/역할 관리 (관리자 전용)
    // =====================================================

    @Transactional
    public void updateGrade(Long userId, UserGrade grade) {
        log.info("등급 변경 요청: userId={}, newGrade={}", userId, grade);

        // BASIC/VIP만 허용
        if (grade != UserGrade.BASIC && grade != UserGrade.VIP) {
            throw new BusinessException(UserErrorCode.INVALID_GRADE);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        user.updateGrade(grade);

        log.info("등급 변경 완료: userId={}, grade={}", userId, grade);
    }

    @Transactional
    public void updateRole(Long userId, UserRole role) {
        log.info("역할 변경 요청: userId={}, newRole={}", userId, role);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        user.updateRole(role);

        log.info("역할 변경 완료: userId={}, role={}", userId, role);
    }

    // =====================================================
    // 구매 관련 (주문 도메인에서 호출)
    // =====================================================

    @Transactional
    public void addPurchaseAmount(Long userId, Long amount) {
        log.info("구매 금액 추가: userId={}, amount={}", userId, amount);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        user.addPurchaseAmount(amount);

        log.info("구매 금액 추가 완료: userId={}, totalAmount={}", userId, user.getTotalPurchaseAmount());

        // 자동 VIP 승급 체크
        checkAndUpgradeToVip(user);
    }

    private void checkAndUpgradeToVip(User user) {
        if (user.getGrade() == UserGrade.BASIC && user.getTotalPurchaseAmount() >= VIP_THRESHOLD) {
            user.updateGrade(UserGrade.VIP);
            log.info("자동 VIP 승급: userId={}, totalAmount={}", user.getId(), user.getTotalPurchaseAmount());
        }
    }

    // =====================================================
    // 검색/필터링 (관리자 전용)
    // =====================================================

    public List<UserSimpleResponse> searchByName(String name) {
        log.debug("이름 검색: name={}", name);

        List<User> users = userRepository.searchByName(name);

        return users.stream()
                .map(userConverter::toSimpleResponse)
                .collect(Collectors.toList());
    }

    public List<UserSimpleResponse> searchByEmail(String email) {
        log.debug("이메일 검색: email={}", email);

        List<User> users = userRepository.searchByEmail(email);

        return users.stream()
                .map(userConverter::toSimpleResponse)
                .collect(Collectors.toList());
    }

    public List<UserSimpleResponse> getUsersByGrade(UserGrade grade) {
        log.debug("등급별 사용자 조회: grade={}", grade);

        List<User> users = userRepository.findByGrade(grade);

        return users.stream()
                .map(userConverter::toSimpleResponse)
                .collect(Collectors.toList());
    }

    public List<UserSimpleResponse> getVipUsers() {
        log.debug("VIP 사용자 목록 조회");

        List<User> users = userRepository.findByGrade(UserGrade.VIP);

        return users.stream()
                .map(userConverter::toSimpleResponse)
                .collect(Collectors.toList());
    }

    public List<UserSimpleResponse> getBasicUsers() {
        log.debug("BASIC 사용자 목록 조회");

        List<User> users = userRepository.findByGrade(UserGrade.BASIC);

        return users.stream()
                .map(userConverter::toSimpleResponse)
                .collect(Collectors.toList());
    }

    // =====================================================
    // 통계 (관리자 대시보드)
    // =====================================================

    public long getActiveUserCount() {
        return userRepository.countByIsActiveTrue();
    }

    public long getBasicUserCount() {
        return userRepository.countByGrade(UserGrade.BASIC);
    }

    public long getVipUserCount() {
        return userRepository.countByGrade(UserGrade.VIP);
    }

    public Long getTotalRevenue() {
        Long total = userRepository.sumTotalPurchaseAmount();
        return total != null ? total : 0L;
    }

    public Double getAveragePurchaseAmount() {
        Double average = userRepository.averagePurchaseAmount();
        return average != null ? average : 0.0;
    }
}
