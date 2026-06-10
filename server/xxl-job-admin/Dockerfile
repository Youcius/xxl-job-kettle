# xxl-job-kettle admin image
# Base: Java 17 (admin uses Spring Boot 2.7; Vue 3 SPA is mounted by compose)
FROM eclipse-temurin:17-jre

LABEL maintainer="xxl-job-kettle"
LABEL description="xxl-job-admin 2.5.0 with Kettle 9.1 integration"

ENV PARAMS=""
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Use the prebuilt fat jar from the Docker build context.
# Build instructions: see DEPLOY.md -> "准备构建产物"
COPY xxl-job-admin-2.5.0.jar /app.jar

EXPOSE 18081

# Kettle script glue writes log files via this JVM property
# Provide sensible defaults that can be overridden via -e PARAMS=...
ENV LOG_HOME=/data/applogs/xxl-job-admin

# JVM options:
#   -DLOG_HOME   : where the admin writes its own log
#   -Xms/-Xmx    : heap sizing
# App options (--xxl.kettle.*) are passed via PARAMS env at runtime
ENTRYPOINT ["sh","-c","java ${LOG_HOME:+-DLOG_HOME=$LOG_HOME} $JAVA_OPTS -jar /app.jar $PARAMS"]
