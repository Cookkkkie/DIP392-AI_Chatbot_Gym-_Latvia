# build
FROM maven:3.9.4-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# run
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/chatbot.jar .
CMD ["java", "-jar", "chatbot.jar"]