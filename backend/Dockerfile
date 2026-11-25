FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
# 先拉取依赖，利用 Docker 层缓存
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
ENV UPLOAD_DIR=/app/upload
ENV JAVA_OPTS="-Xmx512m"
EXPOSE 8080
# 限制 JVM 最大堆内存以适配 2G 容器环境
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]