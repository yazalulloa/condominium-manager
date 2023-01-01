FROM maven:3.8.4-eclipse-temurin-17-alpine as build
COPY src /home/app/src
COPY pom.xml /home/app/
COPY frontend  /home/app/
COPY system.properties /home/app/
RUN mvn -f /home/app/pom.xml clean package -Pproduction

FROM openjdk:17-alpine
WORKDIR /usr/app
COPY --from=build /home/app/target/yaz-condominium-manager-1.0.0.jar /usr/app/app.jar
COPY application.yml /usr/app/
COPY frontend  /usr/app/
COPY system.properties /usr/app/

EXPOSE 8090
ENTRYPOINT ["java","-jar","/usr/app/app.jar"]