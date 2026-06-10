# xxl-job-kettle executor image
#
# The executor runs as a Java 11 process (Spring Boot 2.7 + xxl-job-core 2.5.0).
# When it receives a Kettle job, it FORKS pan.sh / kitchen.sh from the host
# Kettle 9.1 install. Those scripts use $PENTAHO_JAVA_HOME to find the JVM
# they run under. In production we point PENTAHO_JAVA_HOME at the host JDK 8
# (Kettle 9.1 is canonically Java 8). The docker-compose.yml binds the host
# JDK 8 at the same path (/opt/pdi/jdk8) so the value of PENTAHO_JAVA_HOME
# inside the container resolves to a real java executable.
#
# Base: Java 11 (REQUIRED for xxl-job executor + Java 17+ breaks Kettle reflection
# on sun.* classes anyway, but the executor itself runs fine on 11).
FROM eclipse-temurin:11-jre-jammy

LABEL maintainer="xxl-job-kettle"
LABEL description="xxl-job built-in platform executor with Kettle-first scheduling support"

ENV PARAMS=""
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Default PENTAHO_JAVA_HOME points at the container's Java 11, which works for
# dev / staging. Production docker-compose.yml OVERRIDES this to /opt/pdi/jdk8
# (the host Temurin 1.8.0_482 mount).
ENV PENTAHO_JAVA_HOME=$JAVA_HOME

# Required native tools for Kettle script glue:
#   bash       : Kettle 9.1 .sh scripts use bash-isms (arrays, [[ ]], etc.)
#   coreutils  : stdbuf, timeout utilities
RUN apt-get update && apt-get install -y --no-install-recommends \
        bash \
        coreutils \
    && rm -rf /var/lib/apt/lists/*

# Build output jar (configured for appname=xxl-job-executor-kettle, port=9999,
# http callback port 8082, built-in generic handlers, and Kettle-first runtime)
ADD target/xxl-job-executor-sample-springboot-2.5.0.jar /app.jar

# - 8082: Tomcat for httpJobHandler callback (intra-cluster only)
# - 9999: EmbedServer Netty (admin -> executor RPC, intra-cluster only)
EXPOSE 8082 9999

# Mount points expected at runtime (see deploy/docker-compose.yml):
#   -v /opt/pdi/data-integration:/opt/pdi/data-integration:ro   Kettle 9.1 install
#   -v /opt/pdi/jdk8:/opt/pdi/jdk8:ro                            JDK 8 used by pan.sh
#   -v /opt/pdi/logs:/opt/pdi/logs                               Kettle run logs (RW)

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app.jar $PARAMS"]
