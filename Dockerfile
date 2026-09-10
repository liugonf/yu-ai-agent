# 后端镜像：源码构建（上传源码即可，云平台会在构建时执行 Maven 打包）
FROM maven:3.9-amazoncorretto-21 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

# 显式指定本地仓库路径，覆盖本地 .mvn/maven.config 里的 Windows 路径
RUN mvn -q -Dmaven.repo.local=/root/.m2/repository clean package -Dmaven.test.skip=true

FROM openjdk:21-slim
WORKDIR /app

COPY --from=build /app/target/yu-ai-agent-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8124

CMD ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
