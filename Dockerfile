FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw mvnw
RUN chmod +x mvnw
COPY pom.xml .
RUN ./mvnw dependency:resolve -q -P prod
COPY src ./src
RUN ./mvnw package -DskipTests -q -P prod

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN groupadd --system app && useradd --system --gid app --no-create-home app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*   # НОВИЙ РЯДОК
COPY --from=build --chown=app:app /app/target/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=prod
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]