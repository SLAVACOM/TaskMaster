# ---------- BUILD STAGE ----------
FROM gradle:9.0-jdk21 AS build

WORKDIR /app

# Сначала только gradle файлы — кеш зависимостей
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle

RUN ./gradlew dependencies --no-daemon || true

# Потом исходники
COPY src src

RUN ./gradlew bootJar --no-daemon


# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
