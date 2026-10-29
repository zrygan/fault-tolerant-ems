# COMPILE JAVA
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace 

COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle* settings.gradle* ./
RUN chmod +x gradlew

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon > /dev/null

# copies the code
COPY src/ src/
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar --no-daemon -x test && \
    cp build/libs/*.jar app.jar
# -x test skips tests

RUN java -Djarmode=tools -jar app.jar extract --layers --destination extracted

# RUN JAVA
FROM eclipse-temurin:25-jre
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring
USER spring

COPY --from=build /workspace/extracted/dependencies/ ./
COPY --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/extracted/application/ ./

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]