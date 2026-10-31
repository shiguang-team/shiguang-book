# 构建：mvn clean package -DskipTests
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
# 先只拷 pom 拉依赖，利用 Docker 层缓存
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
ENV UPLOAD_DIR=/app/upload
ENV JAVA_OPTS="-Xmx512m"
EXPOSE 8080
# BUG-09 的教训：学生机只有 2G，堆必须限制
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
