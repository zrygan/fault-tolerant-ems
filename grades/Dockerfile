# COMPILE JAVA
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace/grades

COPY grades/gradlew .
COPY grades/gradle/ gradle/
COPY grades/build.gradle* grades/settings.gradle* ./
RUN chmod +x gradlew

RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    ./gradlew dependencies --no-daemon > /dev/null

# copies the code
COPY proto/ /workspace/proto/
COPY grades/src/ src/
RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    ./gradlew bootJar --no-daemon -x test && \
    cp build/libs/*.jar app.jar
# -x test skips tests

RUN java -Djarmode=tools -jar app.jar extract --layers --destination extracted

# RUN JAVA
FROM eclipse-temurin:25-jre
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring
USER spring

COPY --from=build /workspace/grades/extracted/dependencies/ ./
COPY --from=build /workspace/grades/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/grades/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/grades/extracted/application/ ./

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

#only a label, use only what the service used.
# 8080 web service?
EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]