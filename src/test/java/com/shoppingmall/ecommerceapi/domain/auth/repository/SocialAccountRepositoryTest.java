package com.shoppingmall.ecommerceapi.domain.auth.repository;

import com.shoppingmall.ecommerceapi.config.jpa.JpaAuditingConfig;
import com.shoppingmall.ecommerceapi.domain.auth.entity.SocialAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SocialAccountRepositoryTest {

    @Autowired
    SocialAccountRepository repository; // 생성자 주입 X -> ParameterResolutionException 회피 [web:149][web:140]

    @Test
    @DisplayName("findByProviderAndProviderUserId(): provider+providerUserId로 조회된다")
    void findByProviderAndProviderUserId_shouldFind() {
        repository.save(SocialAccount.of("google", "sub-123", 10L, "a@a.com"));

        var found = repository.findByProviderAndProviderUserId("google", "sub-123");

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("findByUserId(): userId로 목록 조회된다")
    void findByUserId_shouldReturnList() {
        repository.save(SocialAccount.of("google", "sub-1", 99L, "g@x.com"));
        repository.save(SocialAccount.of("kakao", "id-2", 99L, "k@x.com"));
        repository.save(SocialAccount.of("naver", "id-3", 100L, "n@x.com"));

        var list = repository.findByUserId(99L);

        assertThat(list).hasSize(2);
    }

    @Test
    @DisplayName("existsByUserIdAndProvider(): 존재 여부가 반환된다")
    void existsByUserIdAndProvider_shouldWork() {
        repository.save(SocialAccount.of("google", "sub-aaa", 7L, null));

        assertThat(repository.existsByUserIdAndProvider(7L, "google")).isTrue();
        assertThat(repository.existsByUserIdAndProvider(7L, "kakao")).isFalse();
    }

    @Test
    @DisplayName("유니크 제약(provider, providerUserId) 중복 저장 시 예외")
    void uniqueConstraint_shouldThrow() {
        repository.save(SocialAccount.of("google", "sub-dup", 1L, "a@a.com"));

        assertThatThrownBy(() ->
                repository.saveAndFlush(SocialAccount.of("google", "sub-dup", 2L, "b@b.com"))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("@CreatedDate: 저장 시 createdAt이 채워진다")
    void createdDate_shouldBeSet() {
        var saved = repository.saveAndFlush(SocialAccount.of("google", "sub-created", 1L, "a@a.com"));
        assertThat(saved.getCreatedAt()).isNotNull();
    }
}
