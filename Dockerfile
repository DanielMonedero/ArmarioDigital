# Multi-stage build for a small final image.
# Uses an image that already ships Maven, so the host doesn't need
# the wrapper scripts (mvnw / .mvn) checked into the repo.

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -ntp -q dependency:go-offline

COPY src ./src
RUN mvn -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN useradd --system --create-home --uid 1001 app && \
    mkdir -p /app/data/images && \
    chown -R app:app /app

COPY --from=build /workspace/target/wardrobe-*.jar /app/app.jar

ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75 -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

USER app

VOLUME ["/app/data/images"]

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
