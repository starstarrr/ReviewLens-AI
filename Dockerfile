# Stage 1: Build the application with Maven and Java 21
FROM maven:3.9.16-eclipse-temurin-21 AS build

WORKDIR /app

# Copy the Maven configuration first to improve dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Run the application with a lightweight Java runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]