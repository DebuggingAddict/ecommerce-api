package com.shoppingmall.ecommerceapi.domain.user.repository;

import com.shoppingmall.ecommerceapi.domain.user.entity.User;
import com.shoppingmall.ecommerceapi.domain.user.entity.enums.UserGrade;
import com.shoppingmall.ecommerceapi.domain.user.entity.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

  // =====================================================
  // 기본 조회
  // =====================================================

  /**
   * 이메일로 사용자 조회
   *
   * @param email 이메일
   * @return User (Optional)
   */
  Optional<User> findByEmail(String email);

  /**
   * 이메일 존재 여부 확인
   *
   * @param email 이메일
   * @return 존재하면 true
   */
  boolean existsByEmail(String email);

  /**
   * ID로 활성 사용자 조회
   *
   * @param id 사용자 ID
   * @return User (Optional)
   */
  @Query("SELECT u FROM User u WHERE u.id = :id AND u.isActive = true AND u.deletedAt IS NULL")
  Optional<User> findActiveById(@Param("id") Long id);

  // =====================================================
  // 활성/비활성 관련
  // =====================================================

  /**
   * 활성 사용자 목록 조회
   *
   * @return 활성 사용자 목록
   */
  List<User> findByIsActiveTrue();

  /**
   * 비활성(탈퇴) 사용자 목록 조회
   *
   * @return 비활성 사용자 목록
   */
  List<User> findByIsActiveFalse();

  /**
   * 활성 사용자 수 조회
   *
   * @return 활성 사용자 수
   */
  long countByIsActiveTrue();

  // =====================================================
  // 등급/역할 관련
  // =====================================================

  /**
   * 특정 등급의 사용자 목록 조회
   *
   * @param grade 사용자 등급
   * @return 사용자 목록
   */
  List<User> findByGrade(UserGrade grade);

  /**
   * 특정 역할의 사용자 목록 조회
   *
   * @param role 사용자 역할
   * @return 사용자 목록
   */
  List<User> findByRole(UserRole role);

  /**
   * VIP 등급 사용자 목록 조회
   *
   * @return VIP 사용자 목록
   */
  @Query("SELECT u FROM User u WHERE u.grade = 'VIP' AND u.isActive = true")
  List<User> findVipUsers();

  // =====================================================
  // 구매 금액 기반 조회
  // =====================================================

  /**
   * 특정 금액 이상 구매한 사용자 목록 조회
   *
   * @param amount 최소 구매 금액
   * @return 사용자 목록
   */
  @Query("SELECT u FROM User u WHERE u.totalPurchaseAmount >= :amount AND u.isActive = true")
  List<User> findByTotalPurchaseAmountGreaterThanEqual(@Param("amount") Long amount);


  // =====================================================
  // 통계/집계
  // =====================================================

  /**
   * 등급별 사용자 수 조회
   *
   * @param grade 사용자 등급
   * @return 사용자 수
   */
  long countByGrade(UserGrade grade);

  /**
   * 역할별 사용자 수 조회
   *
   * @param role 사용자 역할
   * @return 사용자 수
   */
  long countByRole(UserRole role);

  /**
   * 전체 사용자의 총 구매 금액 합계
   *
   * @return 총 구매 금액
   */
  @Query("SELECT SUM(u.totalPurchaseAmount) FROM User u WHERE u.isActive = true")
  Long sumTotalPurchaseAmount();

  /**
   * 평균 구매 금액 조회
   *
   * @return 평균 구매 금액
   */
  @Query("SELECT AVG(u.totalPurchaseAmount) FROM User u WHERE u.isActive = true")
  Double averagePurchaseAmount();

  // =====================================================
  // 검색/필터링 (선택)
  // =====================================================

  /**
   * 이름으로 사용자 검색 (부분 일치)
   *
   * @param name 이름 (일부)
   * @return 사용자 목록
   */
  @Query("SELECT u FROM User u WHERE u.name LIKE %:name% AND u.isActive = true")
  List<User> searchByName(@Param("name") String name);

  /**
   * 이메일로 사용자 검색 (부분 일치)
   *
   * @param email 이메일 (일부)
   * @return 사용자 목록
   */
  @Query("SELECT u FROM User u WHERE u.email LIKE %:email% AND u.isActive = true")
  List<User> searchByEmail(@Param("email") String email);
}
