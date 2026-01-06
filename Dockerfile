# 1. 빌드 단계
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# [수정] 파일들을 먼저 복사합니다.
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# [중요] 복사 직후에 실행 권한을 명시적으로 부여합니다.
RUN chmod +x ./gradlew

# 의존성 미리 다운로드
RUN ./gradlew dependencies --no-daemon || true

# 나머지 소스 코드 복사 및 빌드
COPY . .
RUN ./gradlew clean bootJar -x test --no-daemon

# 2. 실행 단계
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]