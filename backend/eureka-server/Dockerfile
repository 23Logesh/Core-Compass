# ── Stage 1: Build ──────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Cache dependencies first (layer caching optimization)
COPY pom.xml .
COPY ../pom.xml ../pom.xml

COPY src ./src

RUN apk add --no-cache maven && \
    mvn -f pom.xml clean package -DskipTests -q

# ── Stage 2: Run ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/target/*.jar app.jar

# JVM tuning for low-memory Oracle Free Tier (1GB RAM)
ENV JAVA_OPTS="-Xmx256m -Xms128m \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=60 \
    -XX:+OptimizeStringConcat \
    -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8761

HEALTHCHECK --interval=15s --timeout=5s --retries=5 \
    CMD wget -qO- http://localhost:8761/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
