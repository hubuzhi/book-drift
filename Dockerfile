# ---------- 构建阶段：用 Maven 把源码打成 WAR ----------
FROM maven:3.8-openjdk-17 AS build

WORKDIR /build

# 先只拷 pom，利用镜像层缓存：依赖不变时不必重新下载
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# 再拷源码并打包（跳过测试以加快构建）
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- 运行阶段：只保留 Tomcat 与 WAR ----------
FROM tomcat:10.1-jdk17-temurin

# 清理默认示例应用，应用以 ROOT 部署
RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=build /build/target/book-floating.war /usr/local/tomcat/webapps/ROOT.war

# 容器内中文不乱码，时区为上海
ENV LANG=C.UTF-8 \
    TZ=Asia/Shanghai

EXPOSE 8080

# 不依赖 curl：直接用 bash 尝试建立 TCP 连接判断端口是否就绪
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD bash -c 'exec 3<>/dev/tcp/127.0.0.1/8080 || exit 1'

CMD ["catalina.sh", "run"]
