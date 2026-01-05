package com.shoppingmall.ecommerceapi.domain.user.repository;

import com.shoppingmall.ecommerceapi.config.jpa.JpaAuditingConfig;
import com.shoppingmall.ecommerceapi.domain.user.entity.User;
import com.shoppingmall.ecommerceapi.domain.user.entity.enums.UserGrade;
import com.shoppingmall.ecommerceapi.domain.user.entity.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired UserRepository userRepository;
    @Autowired TestEntityManager em;

    private User user(String email, String name, UserRole role, UserGrade grade, long totalPurchaseAmount, boolean isActive, LocalDateTime deletedAt) {
        return User.builder()
                .email(email)
                .name(name)
                .role(role)
                .grade(grade)
                .totalPurchaseAmount(totalPurchaseAmount)
                .isActive(isActive)
                .deletedAt(deletedAt)
                .build();
    }

    private User persist(User u) {
        User saved = em.persistAndFlush(u); // persist + flush
        em.clear(); // detach 후 다시 조회 유도
        return saved;
    }

    @Test
    @DisplayName("findByEmail / existsByEmail: 이메일로 조회 및 존재여부 확인")
    void findByEmail_and_existsByEmail() {
        persist(user("a@test.com", "alice", UserRole.USER, UserGrade.BASIC, 0, true, null));

        Optional<User> found = userRepository.findByEmail("a@test.com");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("alice");

        assertThat(userRepository.existsByEmail("a@test.com")).isTrue();
        assertThat(userRepository.existsByEmail("nope@test.com")).isFalse();
    }

    @Test
    @DisplayName("findActiveById: isActive=true 이고 deletedAt=null 인 경우만 조회된다")
    void findActiveById_filtersByIsActiveAndDeletedAt() {
        User active = persist(user("active@test.com", "active", UserRole.USER, UserGrade.BASIC, 0, true, null));
        User inactive = persist(user("inactive@test.com", "inactive", UserRole.USER, UserGrade.BASIC, 0, false, null));
        User deleted = persist(user("deleted@test.com", "deleted", UserRole.USER, UserGrade.BASIC, 0, true, LocalDateTime.now()));

        assertThat(userRepository.findActiveById(active.getId())).isPresent();
        assertThat(userRepository.findActiveById(inactive.getId())).isEmpty();
        assertThat(userRepository.findActiveById(deleted.getId())).isEmpty();
    }

    @Test
    @DisplayName("findByIsActiveTrue/False + countByIsActiveTrue")
    void activeQueries() {
        persist(user("t1@test.com", "t1", UserRole.USER, UserGrade.BASIC, 0, true, null));
        persist(user("t2@test.com", "t2", UserRole.USER, UserGrade.BASIC, 0, true, null));
        persist(user("f1@test.com", "f1", UserRole.USER, UserGrade.BASIC, 0, false, LocalDateTime.now()));

        List<User> actives = userRepository.findByIsActiveTrue();
        List<User> inactives = userRepository.findByIsActiveFalse();

        assertThat(actives).hasSize(2);
        assertThat(inactives).hasSize(1);
        assertThat(userRepository.countByIsActiveTrue()).isEqualTo(2);
    }

    @Test
    @DisplayName("findByGrade / countByGrade")
    void gradeQueries() {
        persist(user("b1@test.com", "b1", UserRole.USER, UserGrade.BASIC, 0, true, null));
        persist(user("v1@test.com", "v1", UserRole.USER, UserGrade.VIP, 0, true, null));
        persist(user("v2@test.com", "v2", UserRole.USER, UserGrade.VIP, 0, false, null));

        assertThat(userRepository.findByGrade(UserGrade.BASIC)).hasSize(1);
        assertThat(userRepository.findByGrade(UserGrade.VIP)).hasSize(2);
        assertThat(userRepository.countByGrade(UserGrade.VIP)).isEqualTo(2);
    }

    @Test
    @DisplayName("findByRole / countByRole")
    void roleQueries() {
        persist(user("u1@test.com", "u1", UserRole.USER, UserGrade.BASIC, 0, true, null));
        persist(user("a1@test.com", "a1", UserRole.ADMIN, UserGrade.BASIC, 0, true, null));

        assertThat(userRepository.findByRole(UserRole.USER)).hasSize(1);
        assertThat(userRepository.findByRole(UserRole.ADMIN)).hasSize(1);
        assertThat(userRepository.countByRole(UserRole.ADMIN)).isEqualTo(1);
    }

    @Test
    @DisplayName("findVipUsers: grade=VIP AND isActive=true 인 사용자만 조회된다")
    void findVipUsers_onlyActiveVip() {
        persist(user("vip1@test.com", "vip1", UserRole.USER, UserGrade.VIP, 10_000, true, null));
        persist(user("vip2@test.com", "vip2", UserRole.USER, UserGrade.VIP, 20_000, false, null));
        persist(user("basic@test.com", "basic", UserRole.USER, UserGrade.BASIC, 30_000, true, null));

        List<User> vipUsers = userRepository.findVipUsers();
        assertThat(vipUsers).hasSize(1);
        assertThat(vipUsers.get(0).getEmail()).isEqualTo("vip1@test.com");
    }

    @Test
    @DisplayName("findByTotalPurchaseAmountGreaterThanEqual: isActive=true + 구매금액 조건")
    void findByTotalPurchaseAmountGreaterThanEqual_onlyActive() {
        persist(user("p1@test.com", "p1", UserRole.USER, UserGrade.BASIC, 5_000, true, null));
        persist(user("p2@test.com", "p2", UserRole.USER, UserGrade.BASIC, 15_000, true, null));
        persist(user("p3@test.com", "p3", UserRole.USER, UserGrade.BASIC, 50_000, false, null));

        List<User> result = userRepository.findByTotalPurchaseAmountGreaterThanEqual(10_000L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("p2@test.com");
    }

    @Test
    @DisplayName("sumTotalPurchaseAmount / averagePurchaseAmount: 활성 사용자만 집계된다")
    void aggregationQueries_onlyActive() {
        persist(user("s1@test.com", "s1", UserRole.USER, UserGrade.BASIC, 10_000, true, null));
        persist(user("s2@test.com", "s2", UserRole.USER, UserGrade.BASIC, 30_000, true, null));
        persist(user("s3@test.com", "s3", UserRole.USER, UserGrade.BASIC, 100_000, false, null));

        Long sum = userRepository.sumTotalPurchaseAmount();
        Double avg = userRepository.averagePurchaseAmount();

        assertThat(sum).isEqualTo(40_000L);
        assertThat(avg).isEqualTo(20_000.0);
    }

    @Test
    @DisplayName("searchByName: 부분일치 + isActive=true 조건")
    void searchByName_likeQuery_onlyActive() {
        persist(user("n1@test.com", "kim-minsu", UserRole.USER, UserGrade.BASIC, 0, true, null));
        persist(user("n2@test.com", "kim-yeji", UserRole.USER, UserGrade.BASIC, 0, false, null));
        persist(user("n3@test.com", "park", UserRole.USER, UserGrade.BASIC, 0, true, null));

        List<User> result = userRepository.searchByName("kim");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("n1@test.com");
    }

    @Test
    @DisplayName("searchByEmail: 부분일치 + isActive=true 조건")
    void searchByEmail_likeQuery_onlyActive() {
        persist(user("aaa@test.com", "a", UserRole.USER, UserGrade.BASIC, 0, true, null));
        persist(user("bbb@test.com", "b", UserRole.USER, UserGrade.BASIC, 0, false, null));
        persist(user("ccc@sample.com", "c", UserRole.USER, UserGrade.BASIC, 0, true, null));

        List<User> result = userRepository.searchByEmail("test.com");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("aaa@test.com");
    }
}
