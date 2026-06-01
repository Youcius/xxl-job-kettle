FROM eclipse-temurin:11-jre-jammy
LABEL maintainer="xxl-job-kettle"

ENV PARAMS=""
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ADD target/xxl-job-executor-sample-springboot-*.jar /app.jar

EXPOSE 9999
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app.jar $PARAMS"]
