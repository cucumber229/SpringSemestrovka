# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /build
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src ./src
RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
