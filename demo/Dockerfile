# Stage 1: сборка проекта на JDK 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /build

# Копируем mvnw, .mvn и pom.xml, даём mvnw права на исполнение
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Копируем исходники
COPY src ./src

# Собираем проект, полностью пропуская тесты (не компилируем и не запускаем)
RUN ./mvnw -B package -Dmaven.test.skip=true

# Stage 2: runtime (JRE 21)
FROM eclipse-temurin:21-jre
WORKDIR /app

# Копируем собранный JAR из предыдущего stage
COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
