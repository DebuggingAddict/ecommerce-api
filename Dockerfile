# 1. 빌드 단계
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
# gradlew에 실행 권한을 주고 빌드 (JAR 파일 생성)
RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar

# 2. 실행 단계
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# 빌드 단계에서 만들어진 jar 파일만 뽑아서 복사
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]