FROM maven:3.8.4-eclipse-temurin-17-alpine as build
WORKDIR /app
COPY pom.xml .
# To resolve dependencies in a safe way (no re-download when the source code changes)
RUN mvn clean package -Pproduction -Dmaven.main.skip -Dmaven.test.skip && rm -r target

# To package the application
COPY src ./src
RUN mvn clean package -Dmaven.test.skip

FROM openjdk:17-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar .
ENTRYPOINT ["java","-jar","yaz-condominium-manager-1.0.0.jar"]

# COPY src /home/app/src
# COPY pom.xml /home/app/
# COPY frontend  /home/app/
# COPY system.properties /home/app/
# RUN mvn -f /home/app/pom.xml clean package -Pproduction
#
# FROM openjdk:17-alpine
# WORKDIR /usr/app
# COPY --from=build /home/app/target/yaz-condominium-manager-1.0.0.jar /usr/app/app.jar
# COPY application.yml /usr/app/
# COPY frontend  /usr/app/
# COPY system.properties /usr/app/
# ENTRYPOINT ["java","-jar","/usr/app/app.jar"]