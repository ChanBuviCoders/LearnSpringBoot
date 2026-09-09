# ==========================================
# STAGE 1: Build the Executable Jar
# ==========================================
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml first to cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build package
COPY src ./src
RUN mvn clean package -DskipTests --no-transfer-progress

# ==========================================
# STAGE 2: Lightweight Runtime Environment
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy built jar from Stage 1
COPY --from=build /app/target/*.jar app.jar

# Render assigns dynamic ports via $PORT; default exposes 8888
EXPOSE 8888

# Memory tuning to prevent Out-Of-Memory (OOM) crashes on Render Free Tier
# Sets maximum heap size to 384MB, keeping total usage well below 512MB RAM
ENTRYPOINT ["java", "-Xmx384m", "-Xms128m", "-jar", "app.jar"]