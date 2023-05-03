FROM adoptopenjdk/openjdk11:x86_64-alpine-jdk-11.0.19_7-slim
RUN mkdir -p /app
RUN mkdir -p /app/src/main/webapp/assets/css
RUN mkdir -p /app/src/main/webapp/assets/cssc
RUN mkdir -p /app/src/main/webapp/assets/data
RUN mkdir -p /app/src/main/webapp/assets/js
COPY src/main/webapp/assets/css /app/src/main/webapp/assets/css
COPY src/main/webapp/assets/cssc /app/src/main/webapp/assets/cssc
COPY src/main/webapp/assets/data /app/src/main/webapp/assets/data
COPY src/main/webapp/assets/js /app/src/main/webapp/assets/js
RUN mkdir -p /app/datafile
RUN mkdir -p /app/model
COPY datafile/kospi_with_investor.csv /app/datafile
COPY model/Kospi3x3Ep100.mdl /app/model
WORKDIR /app
COPY target/scala-2.13/horus_view.jar horus_view.jar
ENV TZ=Asia/Seoul
ARG DEBIAN_FRONTEND=noninteractive
RUN apk add --update tzdata
ENV JAVA_OPTS=""
ENTRYPOINT ["java", "-cp", "horus_view.jar", "com.yg.JettyLaunchMain"]