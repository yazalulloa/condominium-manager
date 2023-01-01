FROM maven:3.8.4-eclipse-temurin-17-alpine as build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn clean package -Pproduction

FROM openjdk:17-alpine
WORKDIR /usr/app
COPY --from=build /home/app/target/yaz-condominium-manager-1.0.0.jar /usr/app/app.jar
COPY application.yml /usr/app/
EXPOSE 8090
ENTRYPOINT ["java","-jar","/usr/app/app.jar"]