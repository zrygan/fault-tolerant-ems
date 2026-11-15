# COMPILE JAVA
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace/view

COPY view/gradlew .
COPY view/gradle/ gradle/
COPY view/build.gradle* view/settings.gradle* ./
RUN chmod +x gradlew

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon > /dev/null

# copies the code
COPY proto/ /workspace/proto/
COPY view/src/ src/
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

COPY --from=build /workspace/view/extracted/dependencies/ ./
COPY --from=build /workspace/view/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/view/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/view/extracted/application/ ./

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

#only a label, use only what the service used.
# 8080 web service?
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]