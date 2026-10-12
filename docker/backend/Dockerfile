FROM maven:3.8.8-eclipse-temurin-8 AS build
WORKDIR /src
COPY docker/maven/settings.xml /root/.m2/settings.xml
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:8-jre-jammy
WORKDIR /app
RUN mkdir -p /app/uploads
COPY --from=build /src/target/coursearrange-0.0.1-SNAPSHOT.war /app/app.war
EXPOSE 8008
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.war"]
