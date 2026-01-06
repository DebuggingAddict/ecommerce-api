# 1. 빌드 단계
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# 라이브러리 설치 속도를 높이기 위해 gradle 관련 설정만 먼저 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# gradlew 권한 부여 및 의존성 미리 다운로드 (네트워크 403 에러 방지용)
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon || true

# 소스 코드 복사 및 빌드 (테스트 제외로 빌드 시간 단축)
COPY . .
RUN ./gradlew clean bootJar -x test --no-daemon

# 2. 실행 단계
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]