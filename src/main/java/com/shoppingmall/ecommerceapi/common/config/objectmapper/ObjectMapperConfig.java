package com.shoppingmall.ecommerceapi.common.config.objectmapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 커스텀된 ObjectMapper를 빈으로 등록
@Configuration
public class ObjectMapperConfig {

    @Bean
    public ObjectMapper objectMapper(){
        var objectMapper = new ObjectMapper();

        /* == 실무에서 가장 많이 쓰는 ObjectMapper 설정 == */

        // 커스텀해서 쓰도록 설정 : Register Module을 등록해줘야함
        // jdk 17버전 사용 중이지만, 8버전 이후 나온 클래스들 처리하기 위해 [ Jdk8Module 등록 ]
        objectMapper.registerModule(new Jdk8Module()); // jdk 8 버전 이후 클래스 : Optional 처리 등

        objectMapper.registerModule(new JavaTimeModule()); // << local date

        // Unknown 필드 무시 -> 외부 API 응답이나 요청 JSON에 예상치 못한 필드 있어도 에러 내지 않음
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 필드가 없는 빈 객체를 직렬화할때 오류없이(무시하고) JSON으로 처리 -> JPA Proxy 객체 직렬화 시 발생하는 에러 방지
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // 날짜 관련 직렬화 설정
        // Java 날짜 및 시간(LocalDate, LocalDateTime)을 타임스탬프 형식이 아닌 ISO-8601 문자열 형식으로 직렬화하도록 설정
        // 날짜와 시간이 타임스탬프가 아닌 문자열 형식으로 JSON에 기록되도록
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // WRITE_DATES_AS_TIMESTAMPS : 기본이 false

        // 스네이크 케이스 -> API 응답 / 요청을 snake_case로 통일할 때 사용
        objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());

        return objectMapper;
    }
}