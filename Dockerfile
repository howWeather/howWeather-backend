FROM openjdk:17-jdk-slim-buster
ENV TZ=Asia/Seoul
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "/app.jar"]
