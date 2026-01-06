package com.shoppingmall.ecommerceapi.config.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 설정 (분산락 전용)
 * - Spring Data Redis와 분리된 독립 클라이언트
 * - RLock, RSemaphore 등 분산 자료구조용
 */
@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setConnectionPoolSize(50)  // 커넥션 풀 크기
                .setConnectionMinimumIdleSize(10)
                .setRetryAttempts(3)
                .setRetryInterval(1500)
                .setTimeout(3000);

        return Redisson.create(config);
    }
}
