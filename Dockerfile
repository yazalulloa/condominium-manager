FROM maven:3.8.7-eclipse-temurin-17-focal as build
WORKDIR /app
COPY pom.xml .
# To resolve dependencies in a safe way (no re-download when the source code changes)
RUN mvn clean package -Dmaven.main.skip -Dmaven.test.skip -Dspring-boot.repackage.skip=true  && rm -r target

# To package the application
COPY src ./src
COPY frontend ./frontend
COPY package-lock.json .
COPY tsconfig.json .
RUN mvn package -Pproduction

FROM eclipse-temurin:17-jdk-alpine
# FROM openjdk:17-alpine
WORKDIR /app
RUN mkdir -p config
RUN mkdir -p frontend
COPY --from=build /app/target/*.jar .
COPY --from=build /app/frontend/* ./frontend/
COPY application.yml ./config/
COPY verticles.yml ./config/
ENTRYPOINT ["java","-jar","yaz-condominium-manager-1.0.0.jar"]