FROM maven:3.8.7-eclipse-temurin-17-focal as build
WORKDIR /app
COPY pom.xml .
# To resolve dependencies in a safe way (no re-download when the source code changes)
RUN mvn clean package -Dmaven.main.skip -Dmaven.test.skip -Dspring-boot.repackage.skip=true  && rm -r target

# To package the application
COPY src ./src
COPY frontend ./frontend
RUN mvn clean package -Pproduction

FROM openjdk:17-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar .
ENTRYPOINT ["java","-jar","yaz-condominium-manager-1.0.0.jar"]