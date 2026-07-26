# ── 构建阶段 ──
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY .mvn/settings.xml settings.xml
RUN mvn dependency:go-offline -B -s settings.xml
COPY src ./src
RUN mvn clean package -DskipTests -B -s settings.xml

# ── 运行阶段 ──
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/ROOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
