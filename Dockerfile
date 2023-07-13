# FROM maven:3.8.7-eclipse-temurin-17-focal as build
FROM maven:3.9.1-amazoncorretto-20-debian as build

WORKDIR /app
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 --mount=type=cache,target=/root/.vaadin mvn clean package -Dmaven.main.skip -Dmaven.test.skip -Dspring-boot.repackage.skip=true  && rm -r target
#RUN --mount=type=cache,target=/root/.m2,target=/root/.vaadin mvn clean package -Dmaven.main.skip -Dmaven.test.skip -Dspring-boot.repackage.skip=true  && rm -r target

# To resolve dependencies in a safe way (no re-download when the source code changes)
# RUN mvn clean package -Dmaven.main.skip -Dmaven.test.skip -Dspring-boot.repackage.skip=true  && rm -r target

# To package the application
COPY src ./src
COPY frontend ./frontend
# COPY package-lock.json .
# COPY tsconfig.json .
# RUN --mount=type=cache,target=/root/.m2,target=/root/.vaadin mvn package -Pproduction -Dvaadin.force.production.build=true
RUN --mount=type=cache,target=/root/.m2 --mount=type=cache,target=/root/.vaadin mvn package -Pproduction -Dvaadin.force.production.build=true

# native:compile
# FROM eclipse-temurin:17-jdk-alpine
FROM eclipse-temurin:20.0.1_9-jre-alpine
# FROM openjdk:17-alpine
WORKDIR /app
RUN mkdir -p config
RUN mkdir -p frontend
COPY --from=build /app/target/*.jar .

# COPY config/application.yml ./config/
# COPY config/verticles.yml ./config/

# COPY application.yml ./config/
# COPY verticles.yml ./config/

ENTRYPOINT ["java", "--enable-preview" ,"-jar","yaz-condominium-manager-1.0.0.jar"]
